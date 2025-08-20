package org.figuramc.figura_core.script_languages.lua.callback_types.convert;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaBoolean;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaDouble;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.ListView;
import org.figuramc.figura_core.script_hooks.callback.items.StringView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.ListViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.StringViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;

public class CallbackItemToLua implements CallbackType.FromItemVisitor<LuaValue> {

    private final LuaRuntime state;

    public CallbackItemToLua(LuaRuntime state) {
        this.state = state;
    }

    @Override
    public LuaValue visit(CallbackType.Unit __, CallbackItem.Unit item) {
        return Constants.NIL;
    }

    @Override
    public LuaValue visit(CallbackType.Any __, CallbackItem item) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public LuaValue visit(CallbackType.Bool __, CallbackItem.Bool item) {
        return LuaBoolean.valueOf(item.value());
    }

    @Override
    public LuaValue visit(CallbackType.F32 __, CallbackItem.F32 item) {
        return LuaDouble.valueOf(item.value());
    }

    @Override
    public LuaValue visit(CallbackType.F64 __, CallbackItem.F64 item) {
        return LuaDouble.valueOf(item.value());
    }

    @Override
    public LuaValue visit(CallbackType.Str __, StringView item) {
        return StringViewAPI.wrap(item, state);
    }

    @Override
    public LuaValue visit(CallbackType.Entity __, EntityView<?> item) {
        return EntityViewAPI.wrap(item, state);
    }

    @Override
    public <T extends CallbackItem> LuaValue visit(CallbackType.List<T> list, ListView<T> item) {
        return ListViewAPI.wrap(item, state);
    }
}
