package org.figuramc.figura_core.model.texture;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.texture.OwnedMinecraftTexture;
import org.figuramc.figura_translations.TranslatableItems;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class VanillaAvatarTexture extends AvatarTexture {

    private OwnedMinecraftTexture backing;

    protected VanillaAvatarTexture(ModuleMaterials.TextureMaterials.VanillaTexture vanilla) throws AvatarInitError {
        try {
            backing = FiguraConnectionPoint.TEXTURE_PROVIDER.getVanillaTexture(vanilla);
        } catch (IOException e) {
            throw new AvatarInitError(AvatarTexture.RESOURCE_NOT_FOUND, new TranslatableItems.Items1<>(vanilla.resourceLocation().toString()));
        }
    }

    @Override
    public CompletableFuture<Void> commit() {
        return backing.commit();
    }

    @Override
    public void destroy() {
        if (backing != null) {
            backing.destroy();
            backing = null;
        }
    }

    @Override
    public OwnedMinecraftTexture getHandle() {
        return backing;
    }

    @Override
    public Vector4f getUvValues() {
        return new Vector4f(0, 0, 1, 1);
    }

    @Override
    public int getWidth() {
        return backing.width();
    }

    @Override
    public int getHeight() {
        return backing.height();
    }

    @Override
    public int getPixel(int x, int y) {
        return backing.getPixel(x, y);
    }
}
