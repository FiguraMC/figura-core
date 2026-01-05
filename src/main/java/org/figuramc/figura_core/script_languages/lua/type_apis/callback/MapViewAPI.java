package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.MapView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaMapView;

@LuaTypeAPI(typeName = "MapView", wrappedClass = MapView.class)
public class MapViewAPI {

    public static LuaUserdata wrap(MapView<?, ?> mapView, LuaRuntime state) {
        return new LuaUserdata(mapView, state.figuraMetatables.mapView);
    }

    @LuaExpose(name = "new") @LuaPassState
    public static MapView<?, ?> _new(LuaRuntime s, LuaTable map, CallbackType<?> keyType, CallbackType<?> valueType) {
        return new LuaMapView<>(s, map, keyType, valueType);
    }

    @LuaExpose public static void revoke(MapView<?, ?> self) { self.close(); }
    @LuaExpose public static boolean isRevoked(MapView<?, ?> self) { return self.isRevoked(); }
    @LuaExpose public static int size(MapView<?, ?> self) { return self.size(); }

    @LuaExpose @LuaPassState
    public static LuaValue copy(LuaRuntime s, MapView<?, ?> self) throws LuaError, LuaOOM {
        return copyImpl(s, self);
    }

    private static <K extends CallbackItem, V extends CallbackItem> LuaValue copyImpl(LuaRuntime state, MapView<K, V> view) throws LuaError, LuaOOM {
        synchronized (view) {
            LuaTable res = new LuaTable(state.allocationTracker);
            if (view.isRevoked()) return Constants.NIL;
            for (K key : view.keys()) {
                V value = view.get(key);
                LuaValue luaKey = view.keyType().fromItem(state.callbackItemToLua, key);
                if (luaKey.isNil() || luaKey instanceof LuaDouble d && Double.isNaN(d.doubleValue())) continue;
                LuaValue luaValue = view.valueType().fromItem(state.callbackItemToLua, value);
                res.rawset(luaKey, luaValue);
            }
            return res;
        }
    }

}
