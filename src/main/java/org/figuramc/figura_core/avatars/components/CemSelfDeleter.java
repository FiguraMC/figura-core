package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;

import java.util.UUID;

/**
 * Unloads the avatar when its entity is gone.
 * This prevents memory leaks for CEM avatars when their corresponding entity dies or is otherwise removed.
 * May only be applied to Avatar<UUID>, so it can search for the corresponding entity in the world.
 */
public class CemSelfDeleter implements AvatarComponent<CemSelfDeleter> {

    private final Avatar<UUID> self;
    private MinecraftEntity entity = null;

    public static final Type<CemSelfDeleter> TYPE = new Type<>(CemSelfDeleter::new);
    public Type<CemSelfDeleter> getType() { return TYPE; }

    @SuppressWarnings("unchecked") // We verify the key type
    public CemSelfDeleter(Avatar<?> avatar, AvatarModules modules) {
        if (!(avatar.key instanceof UUID)) {
            throw new IllegalArgumentException("Only Avatar<UUID> may have a CemSelfDeleter component!");
        }
        this.self = (Avatar<UUID>) avatar;
    }

    @Override
    public void tick() {
        // Fetch the entity if we don't already have it
        if (entity == null) {
            entity = FiguraConnectionPoint.GAME_DATA_PROVIDER.getEntity(self.key);
            // If we couldn't find the entity, it doesn't exist, so unload the CEM avatar
            if (entity == null) {
                self.unload();
            }
        } else if (entity.isGone()) {
            // Entity is gone now. Unload the CEM avatar
            self.unload();
        }
    }
}
