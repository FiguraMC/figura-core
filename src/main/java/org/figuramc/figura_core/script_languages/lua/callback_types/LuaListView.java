package org.figuramc.figura_core.script_languages.lua.callback_types;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.ListView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

/**
 * This is a ListView *CREATED BY LUA* to be sent elsewhere.
 * This is NOT a ListView someone else created which Lua is accessing.
 * That situation is handled by ListViewAPI.
 */
public class LuaListView<T extends CallbackItem> extends ListView<T> {

    private final int length;
    private LuaRuntime owningState;
    private LuaTable backingTable;
    private CallbackType<T> elementType;

    public LuaListView(LuaRuntime owningState, LuaTable backingTable, CallbackType<T> elementType) {
        super(elementType);
        this.owningState = owningState;
        this.backingTable = backingTable;
        this.elementType = elementType;
        this.length = backingTable.length();
    }

    @Override
    public void revoke() {
        owningState = null;
        backingTable = null;
        elementType = null;
        super.revoke();
    }

    @Override
    public int length() {
        if (isRevoked()) throw new UnsupportedOperationException("TODO error on revoked");
        return length;
    }

    @Override
    public void set(int index, T item) {
        if (isFrozenOrRevoked()) throw new UnsupportedOperationException("TODO error on frozen or revoked");
        if (index < 0 || index >= length) throw new UnsupportedOperationException("TODO error on list out of bounds");
        try {
            backingTable.rawset(index + 1, elementType.fromItem(owningState.callbackItemToLua, item));
        } catch (LuaUncatchableError avatarError) {
            // If this happens it's the callee's fault
            throw new UnsupportedOperationException("TODO Error the LuaListView callee on OOM", avatarError);
        }
    }

    @Override
    public T get(int index) {
        if (isRevoked()) throw new UnsupportedOperationException("TODO error on revoked");
        if (index < 0 || index >= length) throw new UnsupportedOperationException("TODO error on list out of bounds");
        try {
            return elementType.toItem(owningState.luaToCallbackItem, backingTable.rawget(index + 1));
        } catch (LuaError | LuaUncatchableError e) {
            throw new UnsupportedOperationException("TODO Error the LuaListView provider if incorrect element", e);
        }
    }

}
