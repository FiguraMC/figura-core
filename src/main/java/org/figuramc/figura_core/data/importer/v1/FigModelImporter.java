package org.figuramc.figura_core.data.importer.v1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.figuramc.figura_core.data.importer.ModuleImportingException;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.util.IOUtils;
import org.figuramc.figura_core.util.JsonUtils;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.Either;
import org.figuramc.figura_core.util.data_structures.Mutable;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Class with logic to turn a .figmodel JSON object into ModelPartMaterials.
 * Also adds new textures to the list.
 */
public class FigModelImporter {

    // Failed to parse model "%s.figmodel". Likely a bug in the exporter or importer, please report!
    private static final Translatable<TranslatableItems.Items1<String>> INVALID_FIGMODEL
            = Translatable.create("figura_core.error.importing.invalid_figmodel", String.class);

    public static ModuleMaterials.FigmodelMaterials parseFigModel(File rootFile, File modelFile, String fileName, String prefix, JsonObject figmodel, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException {
        try {
            // Process textures and generate a model-local mapping
            ModelLocalTexture[] textureMapping = processTextures(rootFile, modelFile, JsonUtils.getObjectOrEmpty(figmodel, "textures", () -> new RuntimeException("textures must be object")), textures);
            // TODO: custom shaders/materials included in the figmodel
            // Process animations
            LinkedHashMap<String, ModuleMaterials.AnimationMaterials> anims = processAnimations(JsonUtils.getObjectOrEmpty(figmodel, "animations", () -> new RuntimeException("animations must be object")));
            // Process root model parts
            LinkedHashMap<String, ModuleMaterials.ModelPartMaterials> roots = MapUtils.mapValues(figmodel.getAsJsonObject("roots").asMap(), root -> processGroup(root.getAsJsonObject(), textureMapping, materials), LinkedHashMap::new);

            return new ModuleMaterials.FigmodelMaterials(
                    roots,
                    ListUtils.associateByTo(List.of(textureMapping), ModelLocalTexture::name, ModelLocalTexture::globalTextureIndex), // Associate name -> global texture index
                    anims
            );
        } catch (Throwable t) {
            throw new ModuleImportingException(INVALID_FIGMODEL, new TranslatableItems.Items1<>(prefix + "/" + fileName), t);
        }
    }

    // Parse a custom item model
    public static ModuleMaterials.CustomItemModel parseCustomItemModel(File rootFile, File modelFile, String fileName, String prefix, JsonObject figmodel, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException {
        try {
            // First parse the regular model:
            ModuleMaterials.FigmodelMaterials mats = parseFigModel(rootFile, modelFile, fileName, prefix, figmodel, textures, materials);
            // Also parse the transforms map
            LinkedHashMap<String, ModuleMaterials.ItemPartTransform> transforms = new LinkedHashMap<>();
            if (figmodel.has("item_display_data")) {
                JsonObject data = figmodel.getAsJsonObject("item_display_data");
                for (var entry : data.entrySet()) {
                    JsonObject transformJson = entry.getValue().getAsJsonObject();
                    ModuleMaterials.ItemPartTransform transform = new ModuleMaterials.ItemPartTransform(
                            JsonUtils.parseVec3f(transformJson.getAsJsonArray("translation"), Vector3f::new),
                            JsonUtils.parseVec3f(transformJson.getAsJsonArray("rotation"), Vector3f::new),
                            JsonUtils.parseVec3f(transformJson.getAsJsonArray("scale"), () -> new Vector3f(1))
                    );
                    transforms.put(entry.getKey(), transform);
                }
            }
            return new ModuleMaterials.CustomItemModel(mats, transforms);
        } catch (ModuleImportingException ex) {
            throw ex;
        } catch (Throwable t) {
            throw new ModuleImportingException(INVALID_FIGMODEL, new TranslatableItems.Items1<>(prefix + "/" + fileName), t);
        }
    }

    // Return info about textures specific to this model
    private static ModelLocalTexture[] processTextures(File rootFile, File modelFile, JsonObject modelTextures, List<ModuleMaterials.TextureMaterials> allTextures) {
        ModelLocalTexture[] mapping = new ModelLocalTexture[modelTextures.size()];
        int i = -1;
        for (Map.Entry<String, JsonElement> entry : modelTextures.entrySet()) {
            i++;
            String name = IOUtils.stripExtension(entry.getKey(), "png");
            boolean noAtlas = name.endsWith(".noatlas");
            name = IOUtils.stripExtension(name, "noatlas");

            // Find the emissive friend, if any. This is a double for loop with some duplicated work, so not ideal perf wise, but should be decent enough.
            final String name2 = name;
            int emissiveFriendIndex = ListUtils.findIndex(modelTextures.entrySet(), e -> IOUtils.stripExtension(IOUtils.stripExtension(e.getKey(), "png"), "noatlas").equals(name2 + "_e"));
            int normalFriendIndex = ListUtils.findIndex(modelTextures.entrySet(), e -> IOUtils.stripExtension(IOUtils.stripExtension(e.getKey(), "png"), "noatlas").equals(name2 + "_n"));
            int specularFriendIndex = ListUtils.findIndex(modelTextures.entrySet(), e -> IOUtils.stripExtension(IOUtils.stripExtension(e.getKey(), "png"), "noatlas").equals(name2 + "_s"));

            JsonObject texture = entry.getValue().getAsJsonObject();
            // Fetch the UV size.
            Vector2f uvSize = JsonUtils.parseVec2f(texture.getAsJsonArray("uv_size"), () -> {throw new RuntimeException("uv_size must be specified");} );

            // First, check if it's a vanilla texture:
            @Nullable String vanillaTextureOverride = JsonUtils.getStringOrDefault(texture, "vanilla_texture_override", null);
            if (vanillaTextureOverride != null && !vanillaTextureOverride.isBlank()) {
                // Parse into a MinecraftIdentifier.
                var identifier = MinecraftIdentifier.parse(vanillaTextureOverride);
                // TODO: check for invalid identifiers if (identifier == null)
                allTextures.add(new ModuleMaterials.TextureMaterials.VanillaTexture(identifier));
                mapping[i] = new ModelLocalTexture(name, allTextures.size() - 1, uvSize, emissiveFriendIndex, normalFriendIndex, specularFriendIndex, new Mutable<>());
                continue;
            }

            // Second, check if it's a linked texture:
            @Nullable String texPath = JsonUtils.getStringOrDefault(texture, "path", null);
            if (texPath != null && !texPath.isBlank()) {
                Path someKindOfPath = Path.of(texPath); // Path might be relative to the figmodel file's parent folder, or might be absolute
                Path resolvedPath = modelFile.toPath().getParent().resolve(someKindOfPath); // This should have the resolved path

                // Check if this path points to any existing texture
                String location = IOUtils.stringRelativeTo(resolvedPath.toFile(), rootFile);
                int idx = ListUtils.findIndex(allTextures, tex -> tex instanceof ModuleMaterials.TextureMaterials.OwnedTexture owned && location.equals(owned.location()));
                // If it does, defer to that texture.
                if (idx != -1) {
                    mapping[i] = new ModelLocalTexture(name, idx, uvSize, emissiveFriendIndex, normalFriendIndex, specularFriendIndex, new Mutable<>());
                    continue;
                }
            }

            // Finally, it was neither vanilla nor linked, so create a new owned texture.
            byte[] data = Base64.getDecoder().decode(texture.get("png_bytes_base64").getAsString());
            ModuleMaterials.TextureMaterials newTexture = new ModuleMaterials.TextureMaterials.OwnedTexture(null, null, data, noAtlas);

            // Add to list and update mapping.
            allTextures.add(newTexture);
            mapping[i] = new ModelLocalTexture(name, allTextures.size() - 1, uvSize, emissiveFriendIndex, normalFriendIndex, specularFriendIndex, new Mutable<>());
        }
        return mapping;
    }

    private static LinkedHashMap<String, ModuleMaterials.AnimationMaterials> processAnimations(JsonObject animations) {
        return MapUtils.mapValues(animations.asMap(), animElem -> AnimationImporter.parseAnimation(animElem.getAsJsonObject()), LinkedHashMap::new);
    }

    private static ModuleMaterials.ModelPartMaterials processGroup(JsonObject group, ModelLocalTexture[] textureMapping, List<ModuleMaterials.MaterialMaterials> allMaterials) {
        // Structure
        Vector3f origin = JsonUtils.parseVec3f(group.getAsJsonArray("origin"), Vector3f::new);
        Vector3f rotation = JsonUtils.parseVec3f(group.getAsJsonArray("rotation"), Vector3f::new);
        LinkedHashMap<String, ModuleMaterials.ModelPartMaterials> children = MapUtils.mapValues(
                JsonUtils.getObjectOrEmpty(group, "children", () -> new RuntimeException("Group children must be object")).asMap(),
                child -> processGroup(child.getAsJsonObject(), textureMapping, allMaterials),
                LinkedHashMap::new
        );

        // Mimic
        String mimicPart = JsonUtils.getStringOrDefault(group, "mimic_part", null);

        // Rendering
        // TODO: Plan is to make material_index be prioritized, and fallback to texture_index if not specified.
        //       However, material_index will require a lot of work to implement so we can save it for later.
        //       Only support texture_index in figmodel for now and auto-generate the corresponding material

        Integer textureIndex = JsonUtils.getIntOrDefault(group, "texture_index", null);
        Integer materialIndex = null;
        if (textureIndex != null) {
            materialIndex = textureMapping[textureIndex].generatedMaterialIndex().value;
            if (materialIndex == null) {
                List<Either<ModuleMaterials.BuiltinTextureBinding, Integer>> textureBindings = new ArrayList<>();
                textureBindings.add(Either.ofB(textureMapping[textureIndex].globalTextureIndex));
                if (textureMapping[textureIndex].normalFriendLocalIndex != -1)
                    textureBindings.add(Either.ofB(textureMapping[textureMapping[textureIndex].normalFriendLocalIndex].globalTextureIndex));
                if (textureMapping[textureIndex].specularFriendLocalIndex != -1)
                    textureBindings.add(Either.ofB(textureMapping[textureMapping[textureIndex].specularFriendLocalIndex].globalTextureIndex));

                ModuleMaterials.MaterialMaterials newMaterial = new ModuleMaterials.MaterialMaterials(null, Either.ofA(ModuleMaterials.BuiltinShader.BASIC), textureBindings);
                allMaterials.add(newMaterial);
                materialIndex = textureMapping[textureIndex].generatedMaterialIndex().value = allMaterials.size() - 1;
            }
        }

        List<ModuleMaterials.CubeData> cubes = ListUtils.map(group.getAsJsonArray("cubes"),
                cube -> processCube(cube.getAsJsonObject(), textureMapping[Objects.requireNonNull(textureIndex, "Group has cubes but no texture")].uvSize()));
        List<ModuleMaterials.MeshData> meshes = ListUtils.map(group.getAsJsonArray("meshes"),
                mesh -> processMesh(mesh.getAsJsonObject(), textureMapping[Objects.requireNonNull(textureIndex, "Group has meshes but no texture")].uvSize()));

        // Return
        return new ModuleMaterials.ModelPartMaterials(origin, rotation, children, mimicPart, materialIndex, cubes, meshes);
    }

    private static ModuleMaterials.CubeData processCube(JsonObject cube, Vector2f uvSize) {
        Vector3f origin = JsonUtils.parseVec3f(cube.getAsJsonArray("origin"), Vector3f::new);
        Vector3f rotation = JsonUtils.parseVec3f(cube.getAsJsonArray("rotation"), Vector3f::new);
        Vector3f from = JsonUtils.parseVec3f(cube.getAsJsonArray("from"), () -> {throw new RuntimeException("Cube 'from' must be specified");});
        Vector3f to = JsonUtils.parseVec3f(cube.getAsJsonArray("to"), () -> {throw new RuntimeException("Cube 'to' must be specified");});
        Vector3f inflate = JsonUtils.parseVec3f(cube.getAsJsonArray("inflate"), Vector3f::new);
        JsonArray facesArray = cube.getAsJsonArray("faces");
        if (facesArray.size() != 6) throw new IllegalArgumentException("Unexpected # of cube faces - expected 6, got " + facesArray.size());
        @Nullable ModuleMaterials.CubeFace[] faces = facesArray.asList().stream().map(faceElem -> {
            if (faceElem.isJsonNull()) return null;
            JsonObject face = faceElem.getAsJsonObject();
            Vector2f uv_min = JsonUtils.parseVec2f(face.getAsJsonArray("uv_min"), () -> {throw new RuntimeException("Cube face 'uv_min' must be specified");}).div(uvSize);
            Vector2f uv_max = JsonUtils.parseVec2f(face.getAsJsonArray("uv_max"), () -> {throw new RuntimeException("Cube face 'uv_max' must be specified");}).div(uvSize);
            int face_rotation = JsonUtils.getIntOrDefault(face, "rotation", 0);
            if (face_rotation % 90 != 0 || face_rotation < 0 || face_rotation > 270)
                throw new IllegalArgumentException("Unexpected face rotation - expected 0, 90, 180, or 270, got " + face_rotation);
            return new ModuleMaterials.CubeFace(new Vector4f(uv_min.x, uv_min.y, uv_max.x, uv_max.y), face_rotation / 90);
        }).toArray(ModuleMaterials.CubeFace[]::new);
        return new ModuleMaterials.CubeData(origin, rotation, from, to, inflate, faces);
    }

    private static ModuleMaterials.MeshData processMesh(JsonObject mesh, Vector2f uvSize) {
        Vector3f origin = JsonUtils.parseVec3f(mesh.getAsJsonArray("origin"), Vector3f::new);
        Vector3f rotation = JsonUtils.parseVec3f(mesh.getAsJsonArray("rotation"), Vector3f::new);
        JsonArray verticesArray = mesh.getAsJsonArray("vertices");
        List<ModuleMaterials.VertexData> vertices = ListUtils.map(verticesArray, v -> {
            JsonObject vertex = v.getAsJsonObject();
            Vector3f pos = JsonUtils.parseVec3f(vertex.getAsJsonArray("pos"), () -> {throw new RuntimeException("Mesh vertex 'pos' must be specified");});
            // TODO re-add skinning data
            return new ModuleMaterials.VertexData(pos, null);
        });
        // Get face data
        JsonArray facesArray = mesh.getAsJsonArray("faces");
        List<Vector2f> uvs = new ArrayList<>();
        List<Vector4i> indices = new ArrayList<>();
        facesArray.forEach(faceElem -> {
            JsonObject face = faceElem.getAsJsonObject();
            JsonArray faceVertices = face.getAsJsonArray("vertices");
            if (faceVertices.size() < 3 || faceVertices.size() > 4)
                throw new IllegalArgumentException("Unexpected # of mesh face vertices - expected 3 or 4, got " + faceVertices.size());
            Vector4i indicesVec = new Vector4i(-1);
            for (int i = 0; i < faceVertices.size(); i++) {
                JsonObject faceVertex = faceVertices.get(i).getAsJsonObject();
                indicesVec.setComponent(i, faceVertex.get("index").getAsInt());
                uvs.add(JsonUtils.parseVec2f(faceVertex.getAsJsonArray("uv"), () -> {throw new RuntimeException("Mesh face vertex 'uv' must be specified");}).div(uvSize));
            }
            indices.add(indicesVec);
        });
        // Return
        return new ModuleMaterials.MeshData(origin, rotation, vertices, uvs, indices);
    }

    // A model-local texture is mapped to a global texture, and also has a UV size to modify UV values by.
    // UV size is per-model; the same texture may be included in multiple models, with multiple different UV sizes.
    // A texture in a model may also have an associated basic material; used as a fallback if no material is defined on the part but only a texture
    private record ModelLocalTexture(String name, int globalTextureIndex, Vector2f uvSize, int emissiveFriendLocalIndex, int normalFriendLocalIndex, int specularFriendLocalIndex, Mutable<@Nullable Integer> generatedMaterialIndex) {}

}
