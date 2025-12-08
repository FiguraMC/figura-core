package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.items.StringView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaStringView;

@LuaTypeAPI(typeName = "StringView", wrappedClass = StringView.class)
public class StringViewAPI {

    public static LuaUserdata wrap(StringView stringView, LuaRuntime state) {
        return new LuaUserdata(stringView, state.figuraMetatables.stringView);
    }

    @LuaExpose(name = "new")
    public static StringView _new(LuaString luaString) { return new LuaStringView(luaString); }

    @LuaExpose public static void revoke(StringView self) { self.close(); }
    @LuaExpose public static boolean isRevoked(StringView self) { return self.isRevoked(); }
    @LuaExpose public static int length(StringView self) { return self.length(); }

    @LuaExpose @LuaPassState
    public static LuaValue copy(LuaRuntime s, StringView self) throws LuaUncatchableError {
        String copy = self.copy();
        return copy == null ? Constants.NIL : LuaString.valueOf(s.allocationTracker, copy);
    }


}
