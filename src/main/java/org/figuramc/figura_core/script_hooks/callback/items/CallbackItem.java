package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.util.functional.BiThrowingConsumer;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

/**
 * An item that can be sent over a callback safely.
 * This includes between Avatars, and from Java into an Avatar (Like calling an event).
 * It also can be used for translation between modules in different languages.
 * However, not all items support sending across Avatars, as it could lead to memory-hostage issues.
 */
public sealed interface CallbackItem permits
        // Primitives
        CallbackItem.Unit, CallbackItem.Opaque, CallbackItem.Bool, CallbackItem.I32, CallbackItem.F32, CallbackItem.F64,
        // Figura types
        EntityView,
        // Tuples
        CallbackItem.Tuple, CallbackItem.Tuple2, CallbackItem.Tuple3, CallbackItem.Tuple4, CallbackItem.Tuple5, CallbackItem.Tuple6, CallbackItem.Tuple7, CallbackItem.Tuple8,
        // Generics
        CallbackItem.Optional,
        // Generics with many implementations
        ListView, StringView, MapView, FuncView
{

    // Simple constant-sized primitives
    final class Unit implements CallbackItem, Tuple {
        public static final Unit INSTANCE = new Unit();
        private Unit() {}
        @Override public int count() { return 0; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 { }
    }
    // An arbitrary object. Used as a fallback when we don't know what else to convert to.
    // Converting from Opaque -> (Some language) may not always be possible.
    record Opaque(Object value) implements CallbackItem { }
    record Bool(boolean value) implements CallbackItem { }
    record I32(int value) implements CallbackItem { }
    record F32(float value) implements CallbackItem { }
    record F64(double value) implements CallbackItem { }

    // Fixed-size groups of primitives
    sealed interface Tuple extends CallbackItem {
        // Count of items
        int count();
        // Helper to run the consumer on each item in the tuple.
        <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2;
        // Get array
        default CallbackItem[] toArray() {
            CallbackItem[] arr = new CallbackItem[this.count()];
            int[] i = new int[1];
            forEach(item -> arr[i[0]++] = item);
            return arr;
        }
        // Map tuple items to array of R
        default <R, E1 extends Throwable, E2 extends Throwable> R[] map(BiThrowingFunction<CallbackItem, R, E1, E2> mapper, IntFunction<R[]> arrSupplier) throws E1, E2 {
            R[] arr = arrSupplier.apply(this.count());
            int[] i = new int[1];
            this.<E1, E2>forEach(item -> arr[i[0]++] = mapper.apply(item));
            return arr;
        }
    }

    record Tuple2<A extends CallbackItem, B extends CallbackItem>(A a, B b) implements CallbackItem, Tuple {
        @Override public int count() { return 2; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b);
        }
    }
    record Tuple3<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem>(A a, B b, C c) implements CallbackItem, Tuple {
        @Override public int count() { return 3; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c);
        }
    }
    record Tuple4<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem>(A a, B b, C c, D d) implements CallbackItem, Tuple {
        @Override public int count() { return 4; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c); consumer.accept(d);
        }
    }
    record Tuple5<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem>(A a, B b, C c, D d, E e) implements CallbackItem, Tuple {
        @Override public int count() { return 5; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c); consumer.accept(d); consumer.accept(e);
        }
    }
    record Tuple6<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem>(A a, B b, C c, D d, E e, F f) implements CallbackItem, Tuple {
        @Override public int count() { return 6; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c); consumer.accept(d); consumer.accept(e); consumer.accept(f);
        }
    }
    record Tuple7<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem>(A a, B b, C c, D d, E e, F f, G g) implements CallbackItem, Tuple {
        @Override public int count() { return 7; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c); consumer.accept(d); consumer.accept(e); consumer.accept(f); consumer.accept(g);
        }
    }
    record Tuple8<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem>(A a, B b, C c, D d, E e, F f, G g, H h) implements CallbackItem, Tuple {
        @Override public int count() { return 8; }
        @Override public <E1 extends Throwable, E2 extends Throwable> void forEach(BiThrowingConsumer<CallbackItem, E1, E2> consumer) throws E1, E2 {
            consumer.accept(a); consumer.accept(b); consumer.accept(c); consumer.accept(d); consumer.accept(e); consumer.accept(f); consumer.accept(g); consumer.accept(h);
        }
    }

    // Optional (only one implementation)
    record Optional<T extends CallbackItem>(@Nullable T value) implements CallbackItem {}


    // Helper methods to construct tuples out of various args
    static Unit tuple() { return Unit.INSTANCE; }
    static <A extends CallbackItem> A tuple(A a) { return a; }
    static <A extends CallbackItem, B extends CallbackItem> Tuple2<A, B> tuple(A a, B b) { return new Tuple2<>(a, b); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> Tuple3<A, B, C> tuple(A a, B b, C c) { return new Tuple3<>(a, b, c); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> Tuple4<A, B, C, D> tuple(A a, B b, C c, D d) { return new Tuple4<>(a, b, c, d); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> Tuple5<A, B, C, D, E> tuple(A a, B b, C c, D d, E e) { return new Tuple5<>(a, b, c, d, e); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> Tuple6<A, B, C, D, E, F> tuple(A a, B b, C c, D d, E e, F f) { return new Tuple6<>(a, b, c, d, e, f); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> Tuple7<A, B, C, D, E, F, G> tuple(A a, B b, C c, D d, E e, F f, G g) { return new Tuple7<>(a, b, c, d, e, f, g); }
    static <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> Tuple8<A, B, C, D, E, F, G, H> tuple(A a, B b, C c, D d, E e, F f, G g, H h) { return new Tuple8<>(a, b, c, d, e, f, g, h); }

}
