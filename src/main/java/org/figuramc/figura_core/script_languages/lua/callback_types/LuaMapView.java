package org.figuramc.figura_core.script_languages.lua.callback_types;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.MapView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LuaMapView<K extends CallbackItem, V extends CallbackItem> extends MapView<K, V> {

    private LuaRuntime owningState;
    private LuaTable backingTable;

    public LuaMapView(LuaRuntime owningState, LuaTable backingTable, CallbackType<K> keyType, CallbackType<V> valueType) {
        super(keyType, valueType);
        this.owningState = owningState;
        this.backingTable = backingTable;
    }

    @Override public boolean isRevoked() {
        return owningState == null;
    }

    @Override
    public synchronized void close() {
        owningState = null;
        backingTable = null;
        super.close();
    }

    @Override
    public synchronized int size() {
        if (isRevoked()) return -1;
        return backingTable.size();
    }

    @Override
    public synchronized @Nullable V get(K key) {
        if (isRevoked()) return null;
        try {
            LuaValue luaKey = keyType.fromItem(owningState.callbackItemToLua, key);
            // If it's an invalid key, ignore. (Prevent Lua from creating map with unit/nil key type?)
            if (luaKey.isNil() || luaKey instanceof LuaDouble d && Double.isNaN(d.doubleValue())) return null;
            LuaValue luaValue = backingTable.rawget(luaKey);
            if (luaValue.isNil() && valueType != CallbackType.Unit.INSTANCE) return null;
            return valueType.toItem(owningState.luaToCallbackItem, luaValue);
        } catch (LuaError | LuaOOM err) {
            // In case of conversion error: It's the fault of the one who provided the view, they should only have proper V values in the map
            throw new UnsupportedOperationException("TODO Error the LuaMapView provider if incorrect element", err);
        }
    }

    @Override
    public synchronized @Nullable Iterable<K> keys() {
        if (isRevoked()) return null;
        try {
            List<K> keys = new ArrayList<>();
            for (LuaValue k = backingTable.next(Constants.NIL).first(); !k.isNil(); k = backingTable.next(k).first()) {
                keys.add(keyType.toItem(owningState.luaToCallbackItem, k));
            }
            return keys;
        } catch (LuaError | LuaOOM err) {
            // In case of conversion error; Fault lies with the one who provided the view, they should have provided proper K keys in the map
            throw new UnsupportedOperationException("TODO Error the LuaMapView provider if incorrect element", err);
        }
    }
}
