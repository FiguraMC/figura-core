package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue;
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

    @LuaExpose public static void revoke(ListView<?> self) { self.close(); }
    @LuaExpose public static boolean isRevoked(ListView<?> self) { return self.isRevoked(); }
    @LuaExpose public static int length(ListView<?> self) { return self.length(); }

    @LuaExpose @LuaPassState
    public static LuaValue copy(LuaRuntime s, ListView<?> self) throws LuaOOM {
        return copyImpl(s, self);
    }

    private static <T extends CallbackItem> LuaValue copyImpl(LuaRuntime state, ListView<T> view) throws LuaOOM {
        // Be sure to synchronize so it can't be revoked in the middle of copying
        synchronized (view) {
            if (view.isRevoked()) return Constants.NIL;
            int len = view.length();
            if (len == -1) return Constants.NIL;
            LuaTable tab = new LuaTable(len, 0, state.allocationTracker);
            for (int i = 0; i < len; i++)
                tab.rawset(i + 1, view.elementType().fromItem(state.callbackItemToLua, view.get(i)));
            return tab;
        }
    }

}
