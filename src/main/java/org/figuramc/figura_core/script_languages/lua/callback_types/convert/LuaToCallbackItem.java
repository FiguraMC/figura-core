package org.figuramc.figura_core.script_languages.lua.callback_types.convert;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.*;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaListView;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaMapView;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaStringView;

// TODO make conversion errors translatable / have more context?
public class LuaToCallbackItem implements CallbackType.ToItemVisitor<LuaValue, LuaError, LuaOOM> {

    private final LuaRuntime state;

    public LuaToCallbackItem(LuaRuntime state) {
        this.state = state;
    }

    @Override
    public CallbackItem.Unit visit(CallbackType.Unit __, LuaValue value) {
        return CallbackItem.Unit.INSTANCE; // TODO decide, should we typecheck that the value is nil specifically? Or nah?
    }

    @Override
    public CallbackItem.Opaque visit(CallbackType.Opaque __, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackItem.Opaque(value); // Wrap the LuaValue directly into the Opaque.
    }

    @Override
    public CallbackItem visit(CallbackType.Any __, LuaValue value) throws LuaError, LuaOOM {
        return switch (value) {
            case LuaNil nil -> CallbackItem.Unit.INSTANCE;
            case LuaBoolean bool -> new CallbackItem.Bool(bool.value);
            case LuaInteger integer -> new CallbackItem.I32(integer.intValue());
            case LuaDouble number -> new CallbackItem.F64(number.doubleValue());
            case LuaString string -> new LuaStringView(string);
            case LuaFunction func -> new CallbackView<>(new LuaCallback<>(CallbackType.Func.ANY_TO_ANY, state, func));
            case LuaTable table -> new LuaMapView<>(state, table, CallbackType.Any.INSTANCE, CallbackType.Any.INSTANCE); // Table is Any -> Any map
            case LuaUserdata userdata -> switch (userdata.userdata()) {
                case CallbackItem anyItem -> anyItem;
                default -> new CallbackItem.Opaque(value); // Default to opaque object wrapping LuaValue
            };
            default -> new CallbackItem.Opaque(value); // Default to opaque object wrapping LuaValue
        };
    }

    @Override
    public CallbackItem.Bool visit(CallbackType.Bool __, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackItem.Bool(value.toBoolean()); // Use truthiness here? Or typecheck it as a bool? Not sure...
    }

    @Override
    public CallbackItem.I32 visit(CallbackType.I32 __, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackItem.I32(value.checkInteger(state));
    }

    @Override
    public CallbackItem.F32 visit(CallbackType.F32 __, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackItem.F32((float) value.checkDouble(state));
    }

    @Override
    public CallbackItem.F64 visit(CallbackType.F64 __, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackItem.F64(value.checkDouble(state));
    }

    @Override
    public StringView visit(CallbackType.Str __, LuaValue value) throws LuaError, LuaOOM {
        if (value instanceof LuaUserdata userdata && userdata.instance instanceof StringView stringView) return stringView;
        return new LuaStringView(value.checkLuaString(state));
    }

    @Override
    public EntityView<?> visit(CallbackType.Entity __, LuaValue value) throws LuaError, LuaOOM {
        return value.checkUserdata(state, EntityView.class);
    }

    @Override
    public BlockStateView<?> visit(CallbackType.BlockState __, LuaValue value) throws LuaError, LuaOOM {
        return value.checkUserdata(state, BlockStateView.class);
    }

    @Override
    public WorldView<?> visit(CallbackType.World __, LuaValue value) throws LuaError, LuaOOM {
        return value.checkUserdata(state, WorldView.class);
    }

    @Override
    public ItemStackView<?> visit(CallbackType.ItemStack __, LuaValue value) throws LuaError, LuaOOM {
        return value.checkUserdata(state, ItemStackView.class);
    }

    @Override
    public <T extends CallbackItem> ListView<T> visit(CallbackType.List<T> list, LuaValue value) throws LuaError, LuaOOM {
        // TODO figure out list passthrough (what if it's a different type of list? needs typecheck)
        return new LuaListView<>(state, value.checkTable(state), list.element());
    }

    @Override
    public <K extends CallbackItem, V extends CallbackItem> MapView<K, V> visit(CallbackType.Map<K, V> map, LuaValue value) throws LuaError, LuaOOM {
        // TODO figure out map passthrough (what if it's a different type of map? needs typecheck?)
        throw new UnsupportedOperationException("TODO: Lua conversion for MapView");
    }

    @Override
    public <I extends CallbackItem, O extends CallbackItem> CallbackView<I, O> visit(CallbackType.Func<I, O> func, LuaValue value) throws LuaError, LuaOOM {
        return new CallbackView<>(new LuaCallback<>(func, state, value));
    }

    @Override
    public <T extends CallbackItem> CallbackItem.Optional<T> visit(CallbackType.Optional<T> optional, LuaValue value) throws LuaError, LuaOOM {
        if (value.isNil()) return new CallbackItem.Optional<>(null);
        return new CallbackItem.Optional<>(optional.inner().toItem(this, value));
    }

    // Tuples
    @Override
    public <A extends CallbackItem, B extends CallbackItem> CallbackItem.Tuple2<A, B> visit(CallbackType.Tuple2<A, B> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple2<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2))
        );
    }

    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> CallbackItem.Tuple3<A, B, C> visit(CallbackType.Tuple3<A, B, C> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple3<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3))
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> CallbackItem.Tuple4<A, B, C, D> visit(CallbackType.Tuple4<A, B, C, D> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple4<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3)),
                tuple.d().toItem(this, tab.rawget(4))
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> CallbackItem.Tuple5<A, B, C, D, E> visit(CallbackType.Tuple5<A, B, C, D, E> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple5<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3)),
                tuple.d().toItem(this, tab.rawget(4)),
                tuple.e().toItem(this, tab.rawget(5))
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> CallbackItem.Tuple6<A, B, C, D, E, F> visit(CallbackType.Tuple6<A, B, C, D, E, F> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple6<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3)),
                tuple.d().toItem(this, tab.rawget(4)),
                tuple.e().toItem(this, tab.rawget(5)),
                tuple.f().toItem(this, tab.rawget(6))
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> CallbackItem.Tuple7<A, B, C, D, E, F, G> visit(CallbackType.Tuple7<A, B, C, D, E, F, G> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple7<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3)),
                tuple.d().toItem(this, tab.rawget(4)),
                tuple.e().toItem(this, tab.rawget(5)),
                tuple.f().toItem(this, tab.rawget(6)),
                tuple.g().toItem(this, tab.rawget(7))
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> visit(CallbackType.Tuple8<A, B, C, D, E, F, G, H> tuple, LuaValue value) throws LuaError, LuaOOM {
        LuaTable tab = value.checkTable(state);
        return new CallbackItem.Tuple8<>(
                tuple.a().toItem(this, tab.rawget(1)),
                tuple.b().toItem(this, tab.rawget(2)),
                tuple.c().toItem(this, tab.rawget(3)),
                tuple.d().toItem(this, tab.rawget(4)),
                tuple.e().toItem(this, tab.rawget(5)),
                tuple.f().toItem(this, tab.rawget(6)),
                tuple.g().toItem(this, tab.rawget(7)),
                tuple.h().toItem(this, tab.rawget(8))
        );
    }

}
