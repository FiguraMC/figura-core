package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.*;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.TranslatableItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Grants an Avatar access to an Entity.
 * If it's an Avatar<UUID>, the UUID key is used as the entity.
 * Otherwise, it will default to the local player entity.
 * It tracks the concept of "current entity" and can notify when it's changed, for other components to react to.
 * If another component wants to depend on this one's entity change flag, it should use Avatar.assertComponent() during
 * initialize().
 */
public class EntityUser implements AvatarComponent<EntityUser> {

    public static final Type<EntityUser> TYPE = new Type<>(EntityUser::new);
    public Type<EntityUser> getType() { return TYPE; }

    private final UUID uuid; // Constant, set at initialization
    private boolean justChanged; // Tracks whether the entity was just updated. Works as a flag for other components.
    private @Nullable MinecraftEntity entity; // Changes during tick(). Other components access it and use it. (TODO pivot to EntityView idea? To prevent holding on to the entity too long?)

    public EntityUser(Avatar<?> avatar, AvatarModules modules) {
        if (avatar.key instanceof UUID userUUID) this.uuid = userUUID;
        else this.uuid = FiguraConnectionPoint.GAME_DATA_PROVIDER.getLocalUUID();
    }

    // Each tick, maybe update entity
    // If it changed, mark as changed for 1 tick
    @Override
    public void tick() {
        justChanged = false; // Set to false at the beginning
        if (entity == null || entity.isGone()) { // If we don't have the entity...
            // Then search for it!
            lookForEntity();
        }
    }

    private void lookForEntity() {
        MinecraftEntity prevEntity = entity;
        entity = FiguraConnectionPoint.GAME_DATA_PROVIDER.getEntity(uuid);
        // If the entity changed, then say so.
        if (entity != prevEntity)
            justChanged = true;
    }

    public boolean changed() {
        return justChanged;
    }

    public @Nullable MinecraftEntity getEntity() {
        return entity;
    }

}
