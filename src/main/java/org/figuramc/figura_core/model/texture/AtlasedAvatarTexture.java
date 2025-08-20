package org.figuramc.figura_core.model.texture;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.data.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.joml.Vector4f;

import java.util.concurrent.CompletableFuture;

/**
 * An avatar texture that is backed by a FiguraTextureAtlas.
 * The atlas must therefore exist, so we assert it at compile time.
 */
public class AtlasedAvatarTexture extends AvatarTexture {

    // Keep track of where in the atlas we're located
    private final FiguraTextureAtlas.TextureRectangle locationInAtlas;
    // Keep a reference to the atlas through the texture component.
    // This is because the atlas doesn't yet exist when the atlased texture is created.
    private final Textures texturesComponent;

    public AtlasedAvatarTexture(Textures texturesComponent, ModuleMaterials.TextureMaterials.OwnedTexture materials, FiguraTextureAtlas.Builder atlasBuilder) throws AvatarError {
        this.texturesComponent = texturesComponent;
        this.locationInAtlas = atlasBuilder.insert(materials.name(), materials.data());
    }

    @Override public CompletableFuture<Void> commit() {
        assert texturesComponent.atlas != null;
        return texturesComponent.atlas.getHandle().commitRegion(locationInAtlas.getX(), locationInAtlas.getY(), locationInAtlas.getWidth(), locationInAtlas.getHeight());
    }
    @Override public void destroy() { }
    @Override public MinecraftTexture getHandle() {
        assert texturesComponent.atlas != null;
        return this.texturesComponent.atlas.getHandle();
    }
    @Override public Vector4f getUvValues() {
        assert texturesComponent.atlas != null;
        float atlasWidth = (float) texturesComponent.atlas.getWidth();
        float atlasHeight = (float) texturesComponent.atlas.getHeight();
        return new Vector4f(
                locationInAtlas.getX() / atlasWidth,
                locationInAtlas.getY() / atlasHeight,
                locationInAtlas.getWidth() / atlasWidth,
                locationInAtlas.getHeight() / atlasHeight
        );
    }
    @Override public int getWidth() { return this.locationInAtlas.getWidth(); }
    @Override public int getHeight() { return this.locationInAtlas.getHeight(); }
    @Override public int getPixel(int x, int y) {
        assert texturesComponent.atlas != null;
        return texturesComponent.atlas.getPixel(this.locationInAtlas.getX() + x, this.locationInAtlas.getY() + y);
    }

}
