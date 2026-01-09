package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.rendering.shader.BuiltinShader;
import org.figuramc.figura_core.model.rendering.shader.FiguraShader;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.data_structures.Either;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Each model part has a render type and a priority for inheritance.
 * FiguraPartRenderer instances will need to figure out how to handle these.
 */
public record FiguraRenderType(int priority, FiguraShader shader, List<TextureBinding> textureBindings) {

    // Priority determines rendering order. 0 is the default priority.
    // Lower priority values will render first, higher priority values render later.

    // Create from materials
    public static FiguraRenderType from(ModuleMaterials.MaterialMaterials mats, List<AvatarTexture> textures, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocTracker) throws AvatarOutOfMemoryError {
        if (!(mats.shader() instanceof Either.Left(var builtinName))) throw new IllegalStateException("Custom shaders are TODO");

        BuiltinShader shader = switch (builtinName) {
            case BASIC -> BuiltinShader.BASIC;
            case END_PORTAL -> BuiltinShader.END_PORTAL;
            case END_GATEWAY -> BuiltinShader.END_GATEWAY;
        };

        List<TextureBinding> textureBindings = new ArrayList<>();
        for (int i = 0; i < shader.textureBindingPoints.size(); i++) {
            textureBindings.add(getTextureHelper(ListUtils.getOrNull(mats.textureBindings(), i), textures, shader.defaultBindings().get(i)));
        }

        return new FiguraRenderType(0, shader, textureBindings);
    }

    // Helper to either get the TextureBinding or use the fallback
    private static TextureBinding getTextureHelper(@Nullable Either<ModuleMaterials.BuiltinTextureBinding, Integer> binding, List<AvatarTexture> textures, ModuleMaterials.BuiltinTextureBinding fallback) {
        return switch (binding) {
            case null -> {
                var tex = FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(fallback);
                if (tex == null) yield null;
                yield new TextureBinding(tex.a(), tex.b());
            }
            case Either.Left(var builtin) -> {
                var tex = FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(builtin);
                if (tex == null) yield null;
                yield new TextureBinding(tex.a(), tex.b());
            }
            case Either.Right(var texIndex) -> {
                var tex = textures.get(texIndex);
                yield new TextureBinding(tex.getHandle(), tex.getUvValues());
            }
        };
    }

    // Helper
    public static FiguraRenderType basic(int priority, @Nullable AvatarTexture main, @Nullable AvatarTexture normal, @Nullable AvatarTexture specular, @Nullable AvatarTexture lightmap) {
        var mainBinding = main == null ? null : new TextureBinding(main.getHandle(), main.getUvValues());
        var normalBinding = normal == null ? null : new TextureBinding(normal.getHandle(), normal.getUvValues());
        var specularBinding = specular == null ? null : new TextureBinding(specular.getHandle(), specular.getUvValues());
        var lightmapBinding = lightmap == null ? null : new TextureBinding(lightmap.getHandle(), lightmap.getUvValues());
        return new FiguraRenderType(priority, BuiltinShader.BASIC, Arrays.asList(mainBinding, normalBinding, specularBinding, lightmapBinding));
    }

    public record TextureBinding(MinecraftTexture textureHandle, Vector4f uvModifier) {}

}
