package org.figuramc.figura_core.avatars;

import org.figuramc.figura_core.avatars.components.*;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.memory_tracker.AllocationTracker;
import org.figuramc.memory_tracker.AllocationTrackerImpl;

import java.util.List;
import java.util.UUID;

/**
 * Different types of Avatar which have a preset group of components
 */
public class AvatarTemplates {

    // Helper to create an avatar for the local player from the given modules and the player's VanillaModel.
    public static Avatar<UUID> localPlayer(AvatarModules modules, VanillaModel vanillaModel) throws AvatarError {
        // Get items from game data
        UUID uuid = FiguraConnectionPoint.GAME_DATA_PROVIDER.getLocalUUID();
        // Create avatar
        AllocationTracker<AvatarError> allocationTracker = new AllocationTrackerImpl<>(Integer.MAX_VALUE, 0, 0, reason -> { throw new IllegalStateException("TODO OOM errors"); });
        return new Avatar<>(
                AvatarManagers.ENTITIES, uuid,
                modules, allocationTracker,
                List.of(CustomItems.TYPE, EntityRoot.TYPE, EntityUser.TYPE, HudRoot.TYPE, Molang.TYPE, Textures.TYPE, VanillaRendering.TYPE),
                vanillaModel
        );
    }

    // Create a MAIN_GUI avatar from the given modules
    public static Avatar<AvatarManagers.GuiKind> mainGui(AvatarModules modules) throws AvatarError {
        return new Avatar<>(
                AvatarManagers.GUIS, AvatarManagers.GuiKind.MAIN_GUI,
                modules, null,
                List.of(HudRoot.TYPE, Molang.TYPE, Textures.TYPE)
        );
    }

    // Create a CEM avatar for an entity with the given UUID and vanilla model.
    public static Avatar<UUID> cemAvatar(UUID uuid, VanillaModel vanillaModel, AvatarModules modules) throws AvatarError {
        return new Avatar<>(
                AvatarManagers.ENTITIES, uuid,
                modules, null,
                List.of(CemSelfDeleter.TYPE, CustomItems.TYPE, EntityRoot.TYPE, EntityUser.TYPE, Molang.TYPE, Textures.TYPE, VanillaRendering.TYPE),
                vanillaModel
        );
    }
}
