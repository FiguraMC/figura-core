package org.figuramc.figura_core.model.texture;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.concurrent.CompletableFuture;

/**
 * A generic avatar texture as seen from a script.
 * Subclassed by "Standalone", "Atlased", and "Vanilla" variations.
 */
public abstract class AvatarTexture {

    public static final Translatable<TranslatableItems.Items1<String>> INVALID_PNG
            = Translatable.create("figura_core.error.loading.texture.invalid_png", String.class);

    // Create a texture and upload it.
    public static AvatarTexture from(ModuleMaterials.TextureMaterials materials, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker, Textures textureComponent, FiguraTextureAtlas.Builder atlasBuilder) throws AvatarInitError, AvatarOutOfMemoryError {
        switch (materials) {
            case ModuleMaterials.TextureMaterials.OwnedTexture owned -> {
                if (owned.noAtlas()) {
                    return StandaloneAvatarTexture.create(owned, allocationTracker);
                } else {
                    return new AtlasedAvatarTexture(textureComponent, owned, atlasBuilder);
                }
            }
            case ModuleMaterials.TextureMaterials.VanillaTexture vanilla -> {
                throw new UnsupportedOperationException("TODO");
            }
        }
    }

    // Schedule a commit the texture, causing changes to become visible eventually.
    // The changes will be visible when the future is completed.
    public abstract CompletableFuture<Void> commit();
    // Request that any backing native resources are closed.
    // It doesn't have to happen immediately, but it should happen eventually.
    public abstract void destroy();
    // Get the MinecraftTexture handle of a backing texture.
    public abstract MinecraftTexture getHandle();
    // Get UV values that let a part use this texture.
    // x offset, y offset, x scale, y scale. All 0 to 1.
    public abstract Vector4f getUvValues();

    // Width/Height, and ability to get a pixel at a given position
    public abstract int getWidth();
    public abstract int getHeight();
    // Fetch pixel, with highest 8 bits as Alpha and lowest as Red.
    public abstract int getPixel(int x, int y);
}