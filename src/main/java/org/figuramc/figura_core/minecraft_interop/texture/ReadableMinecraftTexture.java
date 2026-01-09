package org.figuramc.figura_core.minecraft_interop.texture;

import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

/**
 * A texture which is mirrored on CPU, so its information is readable
 */
public interface ReadableMinecraftTexture extends MinecraftTexture {

    /**
     * Fetch the pixel at the given x,y value.
     * The highest 8 bits contain Alpha, and the lowest 8 bits contain Red.
     */
    int getPixel(int x, int y);

    @Override
    default <E extends Throwable> ReadableMinecraftTexture makeReadable(@Nullable AllocationTracker<E> allocationTracker) { return this; }
}
