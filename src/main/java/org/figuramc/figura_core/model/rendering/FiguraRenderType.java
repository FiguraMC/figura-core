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
import java.util.List;

/**
 * Each model part has a render type and a priority for inheritance.
 * FiguraPartRenderer instances will need to figure out how to handle these.
 */
public class FiguraRenderType {

    // Bundles a texture handle + a UV modifier
    public record TextureBinding(MinecraftTexture handle, Vector4f uvModifier) {}

    public final int priority;
    public final FiguraShader shader;
    public final List<TextureBinding> textureBindings;

    // Contains only the info needed for draw calls / ordering thereof.
    // - Notably, it strips UV modifier information, since this is only relevant during vertex rebuild
    // Fetch one through the .drawCallInfo() method.
    public record DrawCallInfo(int priority, FiguraShader shader, List<MinecraftTexture> textureHandles) {
        private DrawCallInfo(FiguraRenderType renderType) {
            this(
                    renderType.priority, // Priority needed for ordering
                    renderType.shader, // Shader needed for drawing
                    ListUtils.map(renderType.textureBindings, TextureBinding::handle) // Strip the UV modifier, leaving only the handle
            );
        }
    }

    private @Nullable DrawCallInfo drawCallInfo;
    public DrawCallInfo drawCallInfo() {
        if (this.drawCallInfo == null)
            this.drawCallInfo = new DrawCallInfo(this);
        return this.drawCallInfo;
    }

    public FiguraRenderType(ModuleMaterials.MaterialMaterials mats, List<AvatarTexture> textures, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocTracker) throws AvatarOutOfMemoryError {
        if (!(mats.shader() instanceof Either.Left(var builtinName))) throw new IllegalStateException("Custom shaders are TODO");

        this.priority = 0;

        this.shader = switch (builtinName) {
            case ALBEDO -> BuiltinShader.ALBEDO;
            case ALBEDO_NORMAL -> BuiltinShader.ALBEDO_NORMAL;
            case ALBEDO_SPECULAR -> BuiltinShader.ALBEDO_SPECULAR;
            case ALBEDO_NORMAL_SPECULAR -> BuiltinShader.ALBEDO_NORMAL_SPECULAR;
            case END_PORTAL -> BuiltinShader.END_PORTAL;
            case END_GATEWAY -> BuiltinShader.END_GATEWAY;
        };

        this.textureBindings = new ArrayList<>(shader.textureBindingPoints().size());

        for (int i = 0; i < shader.textureBindingPoints().size(); i++) {
            var binding = ListUtils.getOrNull(mats.textureBindings(), i);
            switch (binding) {
                case null -> textureBindings.add(FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(shader.defaultBindings().get(i)));
                case Either.Left(var builtin) -> textureBindings.add(FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(builtin));
                case Either.Right(var texIndex) -> {
                    var texture = textures.get(texIndex);
                    textureBindings.add(new TextureBinding(texture.getHandle(), texture.getUvValues()));
                }
            }
        }

        assert textureBindings.size() == shader.textureBindingPoints().size();
    }

    public FiguraRenderType(int priority, FiguraShader shader, List<TextureBinding> textureBindings) {
        this.priority = priority;
        this.shader = shader;
        this.textureBindings = textureBindings;

        assert textureBindings.size() == shader.textureBindingPoints().size();
    }

    // Helpers
    public static FiguraRenderType albedo(int priority, AvatarTexture albedo) {
        return new FiguraRenderType(
                priority,
                BuiltinShader.ALBEDO,
                List.of(
                        new TextureBinding(albedo.getHandle(), albedo.getUvValues()),
                        FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(ModuleMaterials.BuiltinTextureBinding.LIGHTMAP)
                )
        );
    }

}
