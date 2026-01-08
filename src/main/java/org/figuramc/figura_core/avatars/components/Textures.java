package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.model.texture.FiguraTextureAtlas;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A component which holds all the textures for an Avatar.
 */
public class Textures implements AvatarComponent<Textures> {

    public static final Type<Textures> TYPE = new Type<>("TEXTURES", Textures::new);
    public Type<Textures> getType() { return TYPE; }

    // The atlas texture. Textures which don't opt out of being atlased go in here to keep things more efficient.
    public final @Nullable FiguraTextureAtlas atlas;
    private final List<List<AvatarTexture>> textures; // textures[moduleIndex][textureIndex]

    // Set this to true once all textures are uploaded
    private volatile boolean ready = false;

    public Textures(Avatar<?> avatar, AvatarModules modules) throws AvatarInitError, AvatarOutOfMemoryError {
        FiguraTextureAtlas.Builder atlasBuilder = FiguraTextureAtlas.builder();
        textures = new ArrayList<>();
        for (var module : modules.loadTimeModules()) {
            ArrayList<AvatarTexture> moduleTextures = new ArrayList<>();
            for (ModuleMaterials.TextureMaterials mats : module.materials.textures())
                moduleTextures.add(AvatarTexture.from(mats, avatar.allocationTracker, this, atlasBuilder));
            textures.add(moduleTextures);
        }
        atlas = atlasBuilder.build(avatar.allocationTracker);

        // Set ready to true once all textures are uploaded
        CompletableFuture<?>[] futures = textures.stream().flatMap(List::stream).map(AvatarTexture::commit).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).thenApply(__ -> this.ready = true);
    }

    public List<AvatarTexture> getTextures(int moduleIndex) {
        return textures.get(moduleIndex);
    }

    public AvatarTexture getTexture(int moduleIndex, int textureIndex) {
        return getTextures(moduleIndex).get(textureIndex);
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void destroy() {
        if (atlas != null) atlas.destroy();
        for (var moduleTextures : textures)
            for (var texture : moduleTextures)
                texture.destroy();
        textures.clear();
    }

}
