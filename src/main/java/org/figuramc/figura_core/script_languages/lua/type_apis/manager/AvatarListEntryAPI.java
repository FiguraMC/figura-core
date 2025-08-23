package org.figuramc.figura_core.script_languages.lua.type_apis.manager;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.avatars.components.ManagerAccess;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;

@LuaTypeAPI(typeName = "AvatarListEntry", wrappedClass = ManagerAccess.AvatarListEntry.class)
public class AvatarListEntryAPI {

    public static LuaUserdata wrap(ManagerAccess.AvatarListEntry obj, LuaRuntime state) {
        return new LuaUserdata(obj, state.figuraMetatables.avatarListEntry);
    }

    // Fetch the error message from parsing this entry's metadata, or nil if parsing didn't fail
    @LuaExpose
    public static @Nullable String error(ManagerAccess.AvatarListEntry self) {
        if (self instanceof ManagerAccess.AvatarListEntry.ImportError err)
            return err.error().getMessage();
        return null;
    }

}
