package org.figuramc.figura_core.model.texture;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.texture.OwnedMinecraftTexture;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * An avatar texture that is backed by its own image, and is not stored as part of an atlas.
 */
public class StandaloneAvatarTexture extends AvatarTexture {

    private OwnedMinecraftTexture backing;

    protected StandaloneAvatarTexture(OwnedMinecraftTexture backing) {
        this.backing = backing;
    }

    // Create the texture (do not upload it yet, since this must be done on the render thread)
    public static StandaloneAvatarTexture create(ModuleMaterials.TextureMaterials.OwnedTexture materials, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarInitError, AvatarOutOfMemoryError {
        try {
            byte[] pngBytes = materials.data();
            // TODO check size of PNG and prevent OOM early if too big
            OwnedMinecraftTexture backingTexture = FiguraConnectionPoint.TEXTURE_PROVIDER.createTextureFromPng(pngBytes);
            if (allocationTracker != null) allocationTracker.track(backingTexture, 4 * backingTexture.width() * backingTexture.height());
            return new StandaloneAvatarTexture(backingTexture);
        } catch (IOException invalidPng) {
            throw new AvatarInitError(AvatarTexture.INVALID_PNG, new TranslatableItems.Items1<>(materials.name()), invalidPng);
        }
    }

    // AbstractAvatarTexture:

    @Override public CompletableFuture<Void> ready() {
        // Wait for the backing texture to be ready; this will also commit the texture for the first time
        return backing.readyToUse();
    }
    @Override public CompletableFuture<Void> commit() {
        return backing.commit();
    }
    @Override public void destroy() {
        if (backing != null) {
            backing.destroy();
            backing = null;
        }
    }
    @Override public OwnedMinecraftTexture getHandle() { return backing; }
    @Override public Vector4f getUvValues() { return new Vector4f(0, 0, 1, 1); }

    @Override public int getWidth() { return backing.width(); }
    @Override public int getHeight() { return backing.height(); }
    @Override public int getPixel(int x, int y) { return backing.getPixel(x, y); }

}
