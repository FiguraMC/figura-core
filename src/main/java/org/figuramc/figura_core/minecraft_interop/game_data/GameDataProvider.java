package org.figuramc.figura_core.minecraft_interop.game_data;

import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
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
     * This should only be called on the main thread.
     * TODO do we care about old enough versions that UUIDs didn't exist yet? No way right?
     */
    @Nullable MinecraftEntity getEntity(UUID uuid);

    // The following functions will be used by the "Client API" to have client information.
    // They should be callable at any point, from any thread!

    float[] getWindowSize(); // Get the size of the Minecraft window in PIXELS. (2 values)
    float[] getScaledWindowSize(); // Get the size of the Minecraft window in GUI UNITS. (2 values)
    float[] getMousePosition(); // Get the mouse position in PIXELS. (2 values)
    float[] getScaledMousePosition(); // Get the mouse position in GUI UNITS. (2 values)

    float getGuiScale(); // Get the GUI scale multiplier.


}
