package org.figuramc.figura_core.minecraft_interop.game_data;

import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A view into the Minecraft game data state.
 */
public interface GameDataProvider {

    /**
     * Get the UUID used for the local player.
     * This UUID can then later be plugged into getEntity() to get the entity instance.
     */
    UUID getLocalUUID();

    /**
     * Fetch the entity with the given UUID, or null if no such entity exists.
     * TODO do we care about old enough versions that UUIDs didn't exist yet? No way right?
     */
    @Nullable MinecraftEntity getEntity(UUID uuid);

    /**
     * Fetch the item with the given string id (namespace:item_name or just item_name for short)
     */
    @Nullable MinecraftItem getItem(String identifier);


}
