package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.ListView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaListView;

@LuaTypeAPI(typeName = "ListView", wrappedClass = ListView.class)
public class ListViewAPI {

    public static LuaUserdata wrap(ListView<?> listView, LuaRuntime state) {
        return new LuaUserdata(listView, state.figuraMetatables.listView);
    }

    @LuaExpose(name = "new") @LuaPassState
    public static ListView<?> _new(LuaRuntime s, LuaTable list, CallbackType<?> elemType) {
        return new LuaListView<>(s, list, elemType);
    }

    @LuaExpose public static void revoke(ListView<?> self) { self.revoke(); }
    @LuaExpose public static boolean isRevoked(ListView<?> self) { return self.isRevoked(); }
    @LuaExpose public static int length(ListView<?> self) { return self.length(); }

    @LuaExpose @LuaPassState
    public static LuaTable copy(LuaRuntime s, ListView<?> self) throws LuaUncatchableError {
        return copyImpl(s, self);
    }

    private static <T extends CallbackItem> LuaTable copyImpl(LuaRuntime state, ListView<T> view) throws LuaUncatchableError {
        int len = view.length();
        LuaTable tab = new LuaTable(len, 0, state.allocationTracker);
        for (int i = 0; i < len; i++)
            tab.rawset(i + 1, view.callbackType.fromItem(state.callbackItemToLua, view.get(i)));
        return tab;
    }

}
