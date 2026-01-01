package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.AvatarTemplates;
import org.figuramc.figura_core.avatars.components.ManagerAccess;
import org.figuramc.figura_core.data.importer.v1.ModuleImporter;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.manager.AvatarListEntryAPI;
import org.figuramc.figura_core.util.data_structures.DataTree;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Creates the 'manager' table for Avatars that are allowed to have it.
 */
public class ManagerTable {

    public static LuaTable create(LuaRuntime state, @NotNull ManagerAccess managerAccess) throws LuaUncatchableError {
        LuaTable manager = new LuaTable(state.allocationTracker);

        // Fetch avatars
        manager.rawset("fetchAvatars", LibFunction.create(s -> {
            DataTree<String, ManagerAccess.AvatarListEntry> avatars = managerAccess.getAvatars();
            if (avatars == null) return new LuaTable(s.allocationTracker);
            return fetchAvatarsRecurse((LuaRuntime) s, avatars);
        }));

        // Load an avatar on the local player.
        // If the arg is nil, will unload the local player's avatar instead.
        manager.rawset("setLocalAvatar", LibFunction.create((s, avatarEntry) -> {
            ManagerAccess.AvatarListEntry entry = avatarEntry.optUserdata(s, ManagerAccess.AvatarListEntry.class, null);
            UUID uuid = FiguraConnectionPoint.GAME_DATA_PROVIDER.getLocalUUID();
            if (entry == null) {
                AvatarManagers.ENTITIES.unload(uuid);
            } else {
                File file = entry.file();
                VanillaModel vanillaModel = FiguraConnectionPoint.GAME_DATA_PROVIDER.getEntity(uuid).getModel();
                AvatarManagers.ENTITIES.load(uuid, () -> {
                    ModuleMaterials materials = ModuleImporter.importFromFile(file);
                    AvatarModules modules = AvatarModules.loadModules(materials);
                    return AvatarTemplates.localPlayer(modules, vanillaModel);
                });
            }
            return Constants.NIL;
        }));

        return manager;
    }

    // Recursive helper for avatar-fetching and generating the tables
    private static LuaTable fetchAvatarsRecurse(LuaRuntime s, DataTree<String, ManagerAccess.AvatarListEntry> tree) throws LuaError, LuaUncatchableError {
        LuaTable result = new LuaTable(s.allocationTracker);
        for (var nodeChild : tree.nodeChildren.entrySet())
            result.rawset(nodeChild.getKey(), fetchAvatarsRecurse(s, nodeChild.getValue()));
        for (var leafChild : tree.leafChildren.entrySet())
            result.rawset(leafChild.getKey(), AvatarListEntryAPI.wrap(leafChild.getValue(), s));
        return result;
    }

}
