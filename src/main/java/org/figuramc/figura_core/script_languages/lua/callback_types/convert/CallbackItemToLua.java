package org.figuramc.figura_core.script_languages.lua.callback_types.convert;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.*;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.FuncViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.ListViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.MapViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.callback.StringViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;

import java.util.function.Function;

public class CallbackItemToLua implements CallbackType.FromItemVisitor<LuaValue, LuaUncatchableError, RuntimeException> {

    private final LuaRuntime state;

    public CallbackItemToLua(LuaRuntime state) {
        this.state = state;
    }

    @Override
    public LuaValue visit(CallbackType.Unit __, CallbackItem.Unit item) {
        return Constants.NIL;
    }

    @Override
    public LuaValue visit(CallbackType.Opaque __, CallbackItem.Opaque item) throws LuaUncatchableError, RuntimeException {
        if (item.value() instanceof LuaValue luaValue) return luaValue;
        throw new UnsupportedOperationException("TODO: Lua Opaque item api");
    }

    @Override
    public LuaValue visit(CallbackType.Any __, CallbackItem item) throws LuaUncatchableError {
        return switch (item) {
            // Primitive
            case CallbackItem.Unit unit -> Constants.NIL;
            case CallbackItem.Opaque opaque -> {
                if (opaque.value() instanceof LuaValue luaValue) yield luaValue;
                throw new UnsupportedOperationException("TODO: Lua Opaque item api");
            }
            case CallbackItem.Bool(boolean value) -> LuaBoolean.valueOf(value);
            case CallbackItem.I32(int value) -> LuaInteger.valueOf(value);
            case CallbackItem.F32(float value) -> LuaDouble.valueOf(value);
            case CallbackItem.F64(double value) -> LuaDouble.valueOf(value);
            case StringView stringView -> StringViewAPI.wrap(stringView, state);
            // Figura objects
            case EntityView<?> entityView -> EntityViewAPI.wrap(entityView, state);
            // Generic
            case CallbackItem.Tuple tuple -> ValueFactory.listOf(state.allocationTracker, tuple.<LuaValue, LuaUncatchableError, RuntimeException>map(inner -> CallbackType.Any.INSTANCE.fromItem(this, inner), LuaValue[]::new));
            case ListView<?> listView -> ListViewAPI.wrap(listView, state);
            case MapView<?, ?> mapView -> MapViewAPI.wrap(mapView, state);
            case FuncView<?, ?> funcView -> FuncViewAPI.wrap(funcView, state);
            case CallbackItem.Optional<?> optional -> optional.value() == null ? Constants.NIL : CallbackType.Any.INSTANCE.fromItem(this, optional.value());
        };
    }

    @Override
    public LuaValue visit(CallbackType.Bool __, CallbackItem.Bool item) {
        return LuaBoolean.valueOf(item.value());
    }

    @Override
    public LuaValue visit(CallbackType.I32 __, CallbackItem.I32 item) {
        return LuaInteger.valueOf(item.value());
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

    @Override
    public <K extends CallbackItem, V extends CallbackItem> LuaValue visit(CallbackType.Map<K, V> type, MapView<K, V> item) throws LuaUncatchableError, RuntimeException {
        throw new UnsupportedOperationException("TODO: Lua conversion for MapView");
    }

    @Override
    public <I extends CallbackItem, O extends CallbackItem> LuaValue visit(CallbackType.Func<I, O> type, FuncView<I, O> item) throws LuaUncatchableError, RuntimeException {
        return FuncViewAPI.wrap(item, state);
    }

    public <T extends CallbackItem> LuaValue visit(CallbackType.Optional<T> optional, CallbackItem.Optional<T> item) throws LuaUncatchableError {
        T val = item.value();
        // Null -> nil
        return val == null ? Constants.NIL : optional.inner().fromItem(this, val);
    }

    // Tuples
    @Override
    public <TupleItem extends CallbackItem.Tuple> LuaValue visitTuple(CallbackType.Tuple<TupleItem> type, TupleItem item) throws LuaUncatchableError, RuntimeException {
        throw new UnsupportedOperationException("visitTuple is not supported on this visitor; instead we impl each tuple separately for type safety.");
    }

    @Override
    public <A extends CallbackItem, B extends CallbackItem> LuaValue visit(CallbackType.Tuple2<A, B> type, CallbackItem.Tuple2<A, B> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> LuaValue visit(CallbackType.Tuple3<A, B, C> type, CallbackItem.Tuple3<A, B, C> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> LuaValue visit(CallbackType.Tuple4<A, B, C, D> type, CallbackItem.Tuple4<A, B, C, D> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c()),
                type.d().fromItem(this, item.d())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> LuaValue visit(CallbackType.Tuple5<A, B, C, D, E> type, CallbackItem.Tuple5<A, B, C, D, E> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c()),
                type.d().fromItem(this, item.d()),
                type.e().fromItem(this, item.e())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> LuaValue visit(CallbackType.Tuple6<A, B, C, D, E, F> type, CallbackItem.Tuple6<A, B, C, D, E, F> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c()),
                type.d().fromItem(this, item.d()),
                type.e().fromItem(this, item.e()),
                type.f().fromItem(this, item.f())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> LuaValue visit(CallbackType.Tuple7<A, B, C, D, E, F, G> type, CallbackItem.Tuple7<A, B, C, D, E, F, G> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c()),
                type.d().fromItem(this, item.d()),
                type.e().fromItem(this, item.e()),
                type.f().fromItem(this, item.f()),
                type.g().fromItem(this, item.g())
        );
    }
    @Override
    public <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> LuaValue visit(CallbackType.Tuple8<A, B, C, D, E, F, G, H> type, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item) throws LuaUncatchableError, RuntimeException {
        return ValueFactory.listOf(state.allocationTracker,
                type.a().fromItem(this, item.a()),
                type.b().fromItem(this, item.b()),
                type.c().fromItem(this, item.c()),
                type.d().fromItem(this, item.d()),
                type.e().fromItem(this, item.e()),
                type.f().fromItem(this, item.f()),
                type.g().fromItem(this, item.g()),
                type.h().fromItem(this, item.h())
        );
    }


}
