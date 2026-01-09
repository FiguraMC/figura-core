package org.figuramc.figura_core.minecraft_interop.texture;

import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A simple handle to a Minecraft texture.
 * For read access, we need ReadableMinecraftTexture.
 * For read/write access, we need OwnedMinecraftTexture.
 */
public interface MinecraftTexture {
    // Get dimensions
    int width();
    int height();

    // Fulfill this future when the texture is ready for use
    CompletableFuture<Void> readyToUse();

    // Make a readable version of this texture, possibly by copying data around.
    // If we do need to copy data around, then be sure to track it with the allocation tracker.
    <E extends Throwable> ReadableMinecraftTexture makeReadable(@Nullable AllocationTracker<E> allocationTracker) throws E;
}
