package org.figuramc.figura_core.minecraft_interop.texture;

import java.util.concurrent.CompletableFuture;

public interface OwnedMinecraftTexture extends ReadableMinecraftTexture {

    /**
     * An owned texture is ready to use after it's been committed once.
     */
    @Override default CompletableFuture<Void> readyToUse() {
        return this.commit();
    }

    /**
     * Schedule a commit for changes made to this texture to the GPU.
     * Return a future which will be fulfilled when the commit is complete.
     */
    CompletableFuture<Void> commit();

    /**
     * Same as commit(), except only for a smaller region of the texture.
     * This may be more efficient if only that region has been edited.
     */
    CompletableFuture<Void> commitRegion(int x, int y, int width, int height);

    /**
     * Set the pixel at the given xy coordinate to the given color.
     */
    void setPixel(int x, int y, int color);

    /**
     * "Paste" the given texture region at the given coordinate in this texture by copying the region.
     */
    void paste(ReadableMinecraftTexture texture, int x, int y, int srcX, int srcY, int width, int height);

    /**
     * Destroy this texture after we're done with it.
     * It doesn't need to destroy it immediately; as long as the resource will be destroyed
     * eventually this is okay.
     */
    void destroy();

}
