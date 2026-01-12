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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4i;

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
    public final ScissorState scissorState;

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

        this.scissorState = new ScissorState();

        assert textureBindings.size() == shader.textureBindingPoints().size();
    }

    public FiguraRenderType(int priority, FiguraShader shader, List<TextureBinding> textureBindings, ScissorState scissorState) {
        this.priority = priority;
        this.shader = shader;
        this.textureBindings = textureBindings;
        this.scissorState = scissorState;
        assert textureBindings.size() == shader.textureBindingPoints().size();
    }

    // Helpers to construct certain render types
    public static FiguraRenderType albedo(int priority, AvatarTexture albedo) {
        return new FiguraRenderType(
                priority,
                BuiltinShader.ALBEDO,
                List.of(
                        new TextureBinding(albedo.getHandle(), albedo.getUvValues()),
                        FiguraConnectionPoint.TEXTURE_PROVIDER.getBuiltinTexture(ModuleMaterials.BuiltinTextureBinding.LIGHTMAP)
                ),
                new ScissorState()
        );
    }

    // Contains only the info needed for draw calls / ordering thereof.
    // - Notably, it strips UV modifier information, since this is only relevant during vertex rebuild
    // Fetch one through the .drawCallInfo() method.
    public record DrawCallInfo(int priority, FiguraShader shader, List<MinecraftTexture> textureHandles, ScissorState scissors) {
        private DrawCallInfo(FiguraRenderType renderType) {
            this(
                    renderType.priority, // Priority needed for ordering
                    renderType.shader, // Shader needed for drawing
                    ListUtils.map(renderType.textureBindings, TextureBinding::handle), // Strip the UV modifier, leaving only the handle
                    renderType.scissorState // Compared through identity equality, so we can't accidentally merge draw calls with different scissor states that happen to have the same values momentarily
            );
        }
    }

    // Mutable object containing scissor state, compared through object identity.
    // An x value of -1 indicates no scissors should be applied.
    public static final class ScissorState {
        private int x = -1, y = -1, w = -1, h = -1;
        public void set(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
        @Contract("_ -> param1")
        public Vector4i get(Vector4i out) {
            return out.set(x, y, w, h);
        }
        // Quick check if the scissor state is active
        public boolean isActive() {
            return x != -1;
        }
        // Compared using object identity; since this is mutable
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    private @Nullable DrawCallInfo drawCallInfo;
    public DrawCallInfo drawCallInfo() {
        if (this.drawCallInfo == null)
            this.drawCallInfo = new DrawCallInfo(this);
        return this.drawCallInfo;
    }

}
