package org.figuramc.figura_core.data.materials;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.util.data_structures.DataTree;
import org.figuramc.figura_core.util.data_structures.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains various types which are used for constructing modules, but can also be serialized.
 * ModuleMaterials acts as a central point for conversions and loading.
 *   --------------------------
 *   |     Module folders     |
 *   --------------------------
 *                |
 *                |
 *                v
 *   --------------------------          -----------------------             -----------------------
 *   |    Module Materials    |  ----->  |   Module Instance   | ---Many---> |   Avatar Instance   |
 *   --------------------------          -----------------------             -----------------------
 *           |         ^
 *           |         |
 *           v         |
 *   --------------------------
 *   |        Raw Bytes       |
 *   --------------------------
 */
public record ModuleMaterials(
        MetadataMaterials metadata,
        DataTree<String, byte[]> scripts,
        List<TextureMaterials> textures, // Use a list because of texture indices for referral
        List<MaterialMaterials> materials, // Use a list because of material indices for referral
        TreeMap<String, ModelPartMaterials> worldRoots,
        @Nullable ModelPartMaterials entityRoot,
        @Nullable ModelPartMaterials hudRoot,
        TreeMap<String, CustomItem> customItemRoots // Tree map for sorted order
) {

    // METADATA
    public record MetadataMaterials(
            // When using scripts, this must be specified.
            // If there are no scripts at all, this will be null.
            @Nullable String language, // "lua" is currently the only valid option
            // For now, dependencies are just strings. TODO improve/make more unique for backend package manager stuff
            // We must maintain the ordering given in the json.
            LinkedHashMap<String, String> dependencies,
            // Whether to automatically require the module's dependencies before initialization. True by default.
            boolean autoRequireDependencies,
            // Exposed API function types
            // We simply maintain the ordering given in the json.
            LinkedHashMap<String, CallbackType.Func<?, ?>> api
    ) {}

    // TEXTURES
    public sealed interface TextureMaterials {
        @Nullable String name(); // Only use if this is a standalone texture!
        record OwnedTexture(@Nullable String name, @Nullable @NoSerialize String location, byte[] data, boolean noAtlas) implements TextureMaterials {}
        record VanillaTexture(MinecraftIdentifier resourceLocation) implements TextureMaterials { @Override public String name() { return null; }}
    }

    // MATERIALS

    // Order matters, used in encoding
    public enum BuiltinShader {
        BASIC, END_PORTAL, END_GATEWAY
    }
    public enum BuiltinTextureBinding {
        NONE, LIGHTMAP, OVERLAY
    }

    public record MaterialMaterials(
        @Nullable String name, // Only use if this is a standalone material (Not implemented yet)
        Either<BuiltinShader, Integer> shader, // Shader for this material; either a builtin or index of a custom
        // Texture bindings:
        // - Null/list too short = default for the shader
        // - Builtin = builtin texture
        // - Integer = texture index in module
        List<@Nullable Either<BuiltinTextureBinding, Integer>> textureBindings
    ) {}

    // MODEL PARTS
    public static class ModelPartMaterials {
        // Structuring
        public final Vector3f origin, rotation;
        public final LinkedHashMap<String, ModelPartMaterials> children;
        // Vanilla part to mimic if any. Legal values depend on the entity, we can't do validation here.
        public final @Nullable String mimic;
        // Rendering data
        public final @Nullable Integer materialIndex; // null means no material; implies no cubes or meshes either
        public final List<CubeData> cubes;
        public final List<MeshData> meshes;
        // Create with all fields
        public ModelPartMaterials(Vector3f origin, Vector3f rotation, LinkedHashMap<String, ModelPartMaterials> children, @Nullable String mimic, @Nullable Integer materialIndex, List<CubeData> cubes, List<MeshData> meshes) {
            this.origin = origin;
            this.rotation = rotation;
            this.children = children;
            this.mimic = mimic;
            this.materialIndex = materialIndex;
            this.cubes = cubes;
            this.meshes = meshes;
        }
        // Constructor to create a simple wrapper around children
        public ModelPartMaterials(LinkedHashMap<String, ModelPartMaterials> children) {
            this(new Vector3f(), new Vector3f(), children, null, null, List.of(), List.of());
        }
    }
    // Correlates to a figmodel
    public static class FigmodelMaterials extends ModelPartMaterials {
        public final LinkedHashMap<String, Integer> textures; // Map names -> texture indices
        // Could potentially introduce a new structure for pre-bound anims using group indices? This could reduce file size slightly from not having to store the slash-separated paths.
        public final LinkedHashMap<String, AnimationMaterials> animations; // Animations

        public FigmodelMaterials(
                LinkedHashMap<String, ModelPartMaterials> children,
                LinkedHashMap<String, Integer> textures,
                LinkedHashMap<String, AnimationMaterials> animations
        ) {
            super(children);
            this.textures = textures;
            this.animations = animations;
        }
    }

    public record CubeData(Vector3f origin, Vector3f rotation, Vector3f from, Vector3f to, Vector3f inflate, @Nullable CubeFace[] faces) {}
    // Vector stores (uv_min.x, uv_min.y, uv_max.x, uv_max.y). UV values are 0-1 (generally speaking; uv values may technically leave the texture). Rot is 0-3.
    public record CubeFace(Vector4f uv, int rot) {}
    public record MeshData(Vector3f origin, Vector3f rotation, List<VertexData> vertices, List<Vector2f> uvs, List<Vector4i> indices) {}
    public record VertexData(Vector3f pos, @Nullable SkinningData skinningData) {}
    public record SkinningData(Vector4i offsets, Vector4f weights) {}

    // CUSTOM ITEMS
    public record CustomItem(@Nullable CustomItemModel model, int textureIndex) {}
    public record CustomItemModel(FigmodelMaterials model, LinkedHashMap<String, ItemPartTransform> transformsByContext) {}
    public record ItemPartTransform(Vector3f translation, Vector3f rotation, Vector3f scale) {}

    // ANIMATIONS
    public record AnimationMaterials(float length, @Nullable Float snapping, float strength, LoopModeMaterials loopMode, TreeMap<String, TransformKeyframesMaterials> transformKeyframes, List<ScriptKeyframeMaterials> scriptKeyframes) {}
    // Keyframe lists must be sorted
    public record TransformKeyframesMaterials(List<TransformKeyframeMaterials> origin, List<TransformKeyframeMaterials> rotation, List<TransformKeyframeMaterials> scale) {}
    // If snapping exists, time is an integer multiple of 1/snapping!
    public record TransformKeyframeMaterials(float time, String x, String y, String z, InterpolationMaterials interpolation) implements Comparable<TransformKeyframeMaterials> {
        @Override public int compareTo(@NotNull ModuleMaterials.TransformKeyframeMaterials o) { return Float.compare(time, o.time); }
    }
    public record ScriptKeyframeMaterials(float time, String code) {} // If snapping exists, time is an integer multiple of 1/snapping

    // Loop mode enum
    public enum LoopModeMaterials {
        ONCE, HOLD, LOOP
    }

    // Interpolation ADT
    public sealed interface InterpolationMaterials {
        final class Linear implements InterpolationMaterials { private Linear() {} public static final Linear INSTANCE = new Linear(); }
        final class CatmullRom implements InterpolationMaterials { private CatmullRom() {} public static final CatmullRom INSTANCE = new CatmullRom(); }
        final class Step implements InterpolationMaterials { private Step() {} public static final Step INSTANCE = new Step(); }
        record Bezier(Vector3f leftTime, Vector3f leftValue, Vector3f rightTime, Vector3f rightValue) implements InterpolationMaterials {}
    }

    // Doesn't do anything, except work as documentation that a certain field should not be serialized.
    // Essentially, it means that this field only exists for convenience during the importing process
    // to make the code cleaner and simpler!
    @Target(ElementType.FIELD)
    private @interface NoSerialize {}

}
