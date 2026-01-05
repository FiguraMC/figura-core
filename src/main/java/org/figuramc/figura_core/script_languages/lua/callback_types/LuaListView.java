package org.figuramc.figura_core.script_languages.lua.callback_types;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.ListView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;

/**
 * This is a ListView *CREATED BY LUA* to be sent elsewhere.
 * This is NOT a ListView someone else created which Lua is accessing.
 * That situation is handled by ListViewAPI.
 */
public final class LuaListView<T extends CallbackItem> extends ListView<T> {

    private final int length;
    private LuaRuntime owningState;
    private LuaTable backingTable;

    public LuaListView(LuaRuntime owningState, LuaTable backingTable, CallbackType<T> elementType) {
        super(elementType);
        this.owningState = owningState;
        this.backingTable = backingTable;
        this.length = backingTable.length();
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
    public synchronized int length() {
        if (isRevoked()) return -1;
        return length;
    }

    @Override
    public synchronized @Nullable T get(int index) {
        if (isRevoked()) return null;
        if (index < 0 || index >= length) throw new UnsupportedOperationException("TODO error on list out of bounds");
        try {
            return elementType.toItem(owningState.luaToCallbackItem, backingTable.rawget(index + 1));
        } catch (LuaError | LuaOOM e) {
            // In case of conversion error: It's the fault of the one who provided the view, they should only have proper T values in the list
            throw new UnsupportedOperationException("TODO Error the LuaListView provider if incorrect element", e);
        }
    }

}
