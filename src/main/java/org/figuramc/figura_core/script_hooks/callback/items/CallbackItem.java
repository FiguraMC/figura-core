package org.figuramc.figura_core.script_hooks.callback.items;

import org.jetbrains.annotations.Nullable;

/**
 * An item that can be sent over a callback safely.
 * This includes between Avatars, and from Java into an Avatar (Like calling an event).
 * It also can be used for translation between modules in different languages.
 * However, not all items support sending across Avatars, as it could lead to memory-hostage issues.
 */
public sealed interface CallbackItem permits
        // Primitives
        CallbackItem.Unit, CallbackItem.Bool, CallbackItem.I32, CallbackItem.F32, CallbackItem.F64,
        // Figura types
        EntityView,
        // Tuples
        CallbackItem.Tuple2, CallbackItem.Tuple3, CallbackItem.Tuple4, CallbackItem.Tuple5, CallbackItem.Tuple6, CallbackItem.Tuple7, CallbackItem.Tuple8,
        // Generics
        CallbackItem.Optional,
        // Generics with many implementations
        ListView, StringView, MapView, FuncView
{

    // Simple constant-sized primitives
    final class Unit implements CallbackItem { public static final Unit INSTANCE = new Unit(); private Unit() {} }
    record Bool(boolean value) implements CallbackItem { }
    record I32(int value) implements CallbackItem { }
    record F32(float value) implements CallbackItem { }
    record F64(double value) implements CallbackItem { }

    // Fixed-size groups of primitives
    record Tuple2<A extends CallbackItem, B extends CallbackItem>(A a, B b) implements CallbackItem {}
    record Tuple3<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem>(A a, B b, C c) implements CallbackItem {}
    record Tuple4<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem>(A a, B b, C c, D d) implements CallbackItem {}
    record Tuple5<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem>(A a, B b, C c, D d, E e) implements CallbackItem {}
    record Tuple6<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem>(A a, B b, C c, D d, E e, F f) implements CallbackItem {}
    record Tuple7<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem>(A a, B b, C c, D d, E e, F f, G g) implements CallbackItem {}
    record Tuple8<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem>(A a, B b, C c, D d, E e, F f, G g, H h) implements CallbackItem {}

    // Optional (only one implementation)
    record Optional<T extends CallbackItem>(@Nullable T value, boolean isPresent) implements CallbackItem {}

}
