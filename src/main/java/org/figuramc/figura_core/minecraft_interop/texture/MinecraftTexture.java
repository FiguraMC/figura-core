package org.figuramc.figura_core.minecraft_interop.texture;

/**
 * A simple handle to a Minecraft texture.
 * For read access, we need ReadableMinecraftTexture.
 * For read/write access, we need OwnedMinecraftTexture.
 */
public interface MinecraftTexture {
    int width();
    int height();
}
