package org.figuramc.figura_core.data.importer.v1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura_core.data.importer.ModuleImportingException;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.script_hooks.ScriptingLanguage;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.CallbackTypeParser;
import org.figuramc.figura_core.util.IOUtils;
import org.figuramc.figura_core.util.JsonUtils;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.DataTree;
import org.figuramc.figura_core.util.data_structures.Mutable;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModuleImporter {

    private static final Translatable<TranslatableItems.Items0> NO_LANGUAGE_SET
            = Translatable.create("figura_core.error.importing.no_language_set");
    private static final Translatable<TranslatableItems.Items0> NO_AVATAR_JSON
            = Translatable.create("figura_core.error.importing.no_avatar_json");
    private static final Translatable<TranslatableItems.Items0> DEPENDENCY_FORMAT
            = Translatable.create("figura_core.error.importing.dependency_format");
    private static final Translatable<TranslatableItems.Items0> API_FORMAT
            = Translatable.create("figura_core.error.importing.api_format");
    private static final Translatable<TranslatableItems.Items1<String>> CONFLICTING_CUSTOM_ITEM_TEXTURES
            = Translatable.create("figura_core.error.importing.conflicting_custom_item_textures", String.class);
    private static final Translatable<TranslatableItems.Items1<String>> CONFLICTING_CUSTOM_ITEM_MODELS
            = Translatable.create("figura_core.error.importing.conflicting_custom_item_models", String.class);

    private static final String METADATA_FILE_NAME = "avatar.json";

    // If the path has metadata, parse it and return.
    // If the path does not have metadata, return null.
    // If it has improperly formatted metadata, throw ModuleImportingException.
    public static @Nullable ModuleMaterials.MetadataMaterials checkMetadata(Path root) throws ModuleImportingException, IOException {
        if (!Files.exists(root.resolve(METADATA_FILE_NAME)))
            return null;
        return readMetadata(root);
    }

    public static ModuleMaterials importPath(Path root) throws ModuleImportingException, IOException {

        var metadata = readMetadata(root);

        // Find scripting language and use it to import
        ScriptingLanguage scriptingLanguage;
        if (metadata.language() != null) {
            // Error if trying to use an unrecognized language
            scriptingLanguage = ScriptingLanguage.LANGUAGES.get(metadata.language());
            if (scriptingLanguage == null)
                throw new ModuleImportingException(ScriptingLanguage.UNKNOWN_LANGUAGE, new TranslatableItems.Items1<>(metadata.language()));
        } else scriptingLanguage = null;

        Path scriptsRoot = root.resolve("scripts");
        DataTree<String, byte[]> scripts;
        if (scriptingLanguage == null) {
            // If no language is set but the module is trying to use scripts anyway, error nicely.
            if (Files.exists(scriptsRoot))
                throw new ModuleImportingException(NO_LANGUAGE_SET, TranslatableItems.Items0.INSTANCE);
            scripts = new DataTree<>();
        } else scripts = scriptingLanguage.findScripts(root);

        // Textures
        Path texturesRoot = root.resolve("textures");
        ArrayList<ModuleMaterials.TextureMaterials> textures = new ArrayList<>();
        IOUtils.recursiveProcess(texturesRoot, p -> readTexture(p, texturesRoot), "png", false, false, IOUtils.getIgnoredFiles(root)).stream().map(Pair::b).forEach(textures::add);

        // Materials (custom are TODO)
        ArrayList<ModuleMaterials.MaterialMaterials> materials = new ArrayList<>();

        // Read custom items first, because they can potentially add new textures which are then used later
        var items = readCustomItems(root, textures, materials);

        @Nullable ModuleMaterials.ModelPartMaterials entity = readRecursiveModel(root, "entity", textures, materials);
        @Nullable ModuleMaterials.ModelPartMaterials hud = readRecursiveModel(root, "hud", textures, materials);

        @Nullable ModuleMaterials.ModelPartMaterials worldOver = readRecursiveModel(root, "world", textures, materials);
        TreeMap<String, ModuleMaterials.ModelPartMaterials> world = worldOver != null ? new TreeMap<>(worldOver.children) : new TreeMap<>();

        return new ModuleMaterials(metadata, scripts, textures, materials, world, entity, hud, items);
    }

    private static ModuleMaterials.MetadataMaterials readMetadata(Path root) throws ModuleImportingException, IOException {
        Path metadataPath = root.resolve(METADATA_FILE_NAME);
        if (!Files.exists(metadataPath)) throw new ModuleImportingException(NO_AVATAR_JSON, TranslatableItems.Items0.INSTANCE);
        String str = Files.readString(metadataPath);
        // If empty, return default materials.
        if (str.isBlank()) return new ModuleMaterials.MetadataMaterials(null, new LinkedHashMap<>(), true, new LinkedHashMap<>());
        // Otherwise, parse as json and read materials.
        JsonObject obj = JsonParser.parseString(str).getAsJsonObject();
        // Parse language:
        @Nullable String language = JsonUtils.getStringOrDefault(obj, "language", null);
        // Auto require dependencies
        boolean autoRequireDependencies = JsonUtils.getBooleanOrDefault(obj, "autoRequireDependencies", true);
        // Parse dependencies:
        LinkedHashMap<String, String> dependencies;
        {
            JsonElement e = obj.get("dependencies");
            if (e == null) {
                dependencies = new LinkedHashMap<>();
            } else if (e.isJsonObject()) {
                dependencies = new LinkedHashMap<>();
                for (var entry : e.getAsJsonObject().entrySet()) {
                    String name = entry.getKey();
                    JsonElement dep = entry.getValue();
                    if (!dep.isJsonPrimitive() || !dep.getAsJsonPrimitive().isString())
                        throw new ModuleImportingException(DEPENDENCY_FORMAT, TranslatableItems.Items0.INSTANCE);
                    String depString = dep.getAsString();
                    dependencies.put(name, depString);
                }
            } else throw new ModuleImportingException(DEPENDENCY_FORMAT, TranslatableItems.Items0.INSTANCE);
        }
        // Parse API:
        LinkedHashMap<String, CallbackType.Func<?, ?>> api;
        {
            JsonElement e = obj.get("api");
            if (e == null) api = new LinkedHashMap<>();
            else if (e.isJsonObject()) {
                api = new LinkedHashMap<>();
                for (var entry : e.getAsJsonObject().entrySet()) {
                    if (!(entry.getValue() instanceof JsonPrimitive prim && prim.isString()))
                        throw new ModuleImportingException(API_FORMAT, TranslatableItems.Items0.INSTANCE);
                    String s = prim.getAsString();
                    CallbackType<?> ty = CallbackTypeParser.parse(s);
                    if (!(ty instanceof CallbackType.Func<?,?> func))
                        throw new ModuleImportingException(API_FORMAT, TranslatableItems.Items0.INSTANCE);
                    api.put(entry.getKey(), func);
                }
            } else throw new ModuleImportingException(API_FORMAT, TranslatableItems.Items0.INSTANCE);
        }
        return new ModuleMaterials.MetadataMaterials(language, dependencies, autoRequireDependencies, api);
    }

    private static ModuleMaterials.TextureMaterials readTexture(Path path, Path texturesRoot) throws IOException {
        String name = IOUtils.stringRelativeTo(path, texturesRoot);
        name = IOUtils.stripExtension(name, "png");
        boolean noAtlas = name.endsWith(".noatlas");
        if (noAtlas) name = name.substring(0, name.length() - ".noatlas".length());
        byte[] data = Files.readAllBytes(path);
        return new ModuleMaterials.TextureMaterials.OwnedTexture(name, path, data, noAtlas);
    }

    private static @Nullable ModuleMaterials.ModelPartMaterials readRecursiveModel(Path root, String name, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException, IOException {
        return IOUtils.recursiveProcess(root.resolve(name),
                figmodel -> readFigModel(root, figmodel, textures, materials),
                (folder, models) -> new ModuleMaterials.ModelPartMaterials(models),
                "figmodel", true, IOUtils.getIgnoredFiles(root)
        );
    }

    private static TreeMap<String, ModuleMaterials.CustomItem> readCustomItems(Path root, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException, IOException {
        Map<String, MutablePair<ModuleMaterials.@Nullable CustomItemModel, Integer>> pairs = new TreeMap<>(); // Treemap for sorted keys, consistency
        // Recursive process, with no return value, just mutates the pairs.
        // TODO should the folder structure matter for this at all?
        IOUtils.<Void, ModuleImportingException>recursiveProcess(root.resolve("items"),
                path -> {
                    if (path.toFile().getName().endsWith(".png")) {
                        // Get pattern and pair
                        String pattern = path.toFile().getName().substring(0, path.toFile().getName().length() - ".png".length());
                        var pair = pairs.computeIfAbsent(pattern, x -> new MutablePair<>(null, -1));
                        // If we already have a texture for this pattern, error out!
                        if (pair.b != -1) throw new ModuleImportingException(CONFLICTING_CUSTOM_ITEM_TEXTURES, new TranslatableItems.Items1<>(pattern));
                        // Figure out what int to give. If the tex already exists, reuse it, otherwise read a new one
                        int alreadyExists = ListUtils.findIndex(textures, tex -> tex instanceof ModuleMaterials.TextureMaterials.OwnedTexture owned && path.equals(owned.path()));
                        if (alreadyExists != -1) {
                            pair.b = alreadyExists;
                        } else {
                            textures.add(readTexture(path, root));
                            pair.b = textures.size() - 1;
                        }
                    } else if (path.toFile().getName().endsWith(".figmodel")) {
                        // Get pattern and pair
                        String pattern = path.toFile().getName().substring(0, path.toFile().getName().length() - ".figmodel".length());
                        var pair = pairs.computeIfAbsent(pattern, x -> new MutablePair<>(null, -1));
                        // If we already have a model for this pattern, error out!
                        if (pair.a != null) throw new ModuleImportingException(CONFLICTING_CUSTOM_ITEM_MODELS, new TranslatableItems.Items1<>(pattern));
                        // Parse the model and store in pair
                        @Nullable ModuleMaterials.CustomItemModel model = readCustomItemModel(root, path, textures, materials);
                        assert model != null;
                        pair.a = model;
                    }
                    return null;
                }, (p, m) -> null, IOUtils.getIgnoredFiles(root));
        // Map values and return
        return MapUtils.mapValues(pairs, pair -> new ModuleMaterials.CustomItem(pair.a, pair.b), TreeMap::new);
    }

    private static @Nullable ModuleMaterials.ModelPartMaterials readFigModel(Path root, Path path, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException, IOException {
        if (!path.toString().endsWith(".figmodel")) return null;
        String fullName = IOUtils.stringRelativeTo(path, root);
        int lastSlash = fullName.lastIndexOf('/');
        String prefix = fullName.substring(0, lastSlash);
        String fileName = fullName.substring(lastSlash + 1, fullName.length() - ".figmodel".length());
        JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        return FigModelImporter.parseFigModel(fileName, prefix, json, textures, materials);
    }

    private static @Nullable ModuleMaterials.CustomItemModel readCustomItemModel(Path root, Path path, ArrayList<ModuleMaterials.TextureMaterials> textures, ArrayList<ModuleMaterials.MaterialMaterials> materials) throws ModuleImportingException, IOException {
        if (!path.toString().endsWith(".figmodel")) return null;
        String fullName = IOUtils.stringRelativeTo(path, root);
        int lastSlash = fullName.lastIndexOf('/');
        assert lastSlash != -1;
        String prefix = fullName.substring(0, lastSlash);
        String fileName = fullName.substring(lastSlash + 1, fullName.length() - ".figmodel".length());
        JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        return FigModelImporter.parseCustomItemModel(fileName, prefix, json, textures, materials);
    }

    private static class MutablePair<A, B> {
        private A a;
        private B b;
        public MutablePair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }

}
