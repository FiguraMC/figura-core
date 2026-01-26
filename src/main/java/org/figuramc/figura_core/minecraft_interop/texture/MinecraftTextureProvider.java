package org.figuramc.figura_core.minecraft_interop.texture;

import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Provide the ability to create textures, and also ability to fetch existing textures.
 */
public interface MinecraftTextureProvider {
    /**
     * Create a new blank texture (zeroed) of the given size.
     * The texture shouldn't necessarily be uploaded until commit() is called.
     * Note that this method might not be called from the render thread!
     */
    OwnedMinecraftTexture createBlankTexture(int width, int height);
    /**
     * Interpret the given bytes as a PNG and create a texture from it.
     * Throw an IOException if the PNG import fails.
     * The texture shouldn't necessarily be uploaded until commit() is called.
     * Note that this method might not be called from the render thread!
     */
    OwnedMinecraftTexture createTextureFromPng(byte[] pngBytes) throws IOException;

    /**
     * Returns a reference to one of the renderer's builtin textures. Used in shaders.
     * Should not be fallible; this is core.
     * Note that this method might not be called from the render thread!
     */
    FiguraRenderType.TextureBinding getBuiltinTexture(ModuleMaterials.BuiltinTextureBinding builtin);
}
