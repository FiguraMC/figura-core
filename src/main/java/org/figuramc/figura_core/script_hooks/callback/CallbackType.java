package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.script_hooks.callback.items.*;
import org.figuramc.figura_core.util.functional.BiThrowingConsumer;
import org.figuramc.memory_tracker.AllocationTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

// Types that can be sent through callbacks; statically checked and converted.
// Correlated through the generic to the corresponding CallbackItem.
public sealed interface CallbackType<T extends CallbackItem> {

    <Outside, E1 extends Throwable, E2 extends Throwable> T toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2;
    <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, T item) throws E1, E2;

    default String stringify() { return fromItem(StringifyVisitor.INSTANCE, null); }
    default int getSize() { return fromItem(SizeVisitor.INSTANCE, null); }

    // Max supported tuple size
    int MAX_TUPLE_SIZE = 8;

    sealed interface Tuple<TupleItem extends CallbackItem> extends CallbackType<TupleItem> {
        int count(); // Number of items in the tuple
        // Careful with toItems; the array's length must be at least count, or else array index out of bounds exception
        <Outside, E1 extends Throwable, E2 extends Throwable> TupleItem toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] items) throws E1, E2;
        <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, TupleItem items) throws E1, E2;
    }

    // "Void" / "Nothing" / null
    final class Unit implements CallbackType<CallbackItem.Unit>, Tuple<CallbackItem.Unit> {
        public static final Unit INSTANCE = new Unit(); private Unit() {}
        @Override public int count() { return 0; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Unit toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Unit item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Unit toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] items) { return CallbackItem.Unit.INSTANCE; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Unit item) { return java.util.List.of(); }
    }

    // A type similar to Any, but it specifically represents something that wasn't convertible into another callback type.
    final class Opaque implements CallbackType<CallbackItem.Opaque> {
        public static final Opaque INSTANCE = new Opaque(); private Opaque() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Opaque toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Opaque item) throws E1, E2 { return visitor.visit(this, item); }
    }

    // Primitives
    final class Any implements CallbackType<CallbackItem> {
        public static final Any INSTANCE = new Any(); private Any() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem item) throws E1, E2 { return visitor.visit(this, item); }
    }
    final class Bool implements CallbackType<CallbackItem.Bool> {
        public static final Bool INSTANCE = new Bool(); private Bool() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Bool toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Bool item) throws E1, E2 { return visitor.visit(this, item); }
    }
    final class I32 implements CallbackType<CallbackItem.I32> {
        public static final I32 INSTANCE = new I32(); private I32() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.I32 toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.I32 item) throws E1, E2 { return visitor.visit(this, item); }
    }
    final class F32 implements CallbackType<CallbackItem.F32> {
        public static final F32 INSTANCE = new F32(); private F32() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.F32 toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.F32 item) throws E1, E2 { return visitor.visit(this, item); }
    }
    final class F64 implements CallbackType<CallbackItem.F64> {
        public static final F64 INSTANCE = new F64(); private F64() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.F64 toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.F64 item) throws E1, E2 { return visitor.visit(this, item); }
    }
    final class Str implements CallbackType<StringView> {
        public static final Str INSTANCE = new Str(); private Str() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> StringView toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, StringView item) throws E1, E2 { return visitor.visit(this, item); }
    }

    // Figura items
    final class Entity implements CallbackType<EntityView<?>> {
        public static final Entity INSTANCE = new Entity(); private Entity() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> EntityView<?> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, EntityView<?> item) throws E1, E2 { return visitor.visit(this, item); }
    }

    // Objects
//    final class FiguraPart implements CallbackType { public static final FiguraPart INSTANCE = new FiguraPart(); private FiguraPart() {} }

    // Generic types
    record List<Item extends CallbackItem>(CallbackType<Item> element) implements CallbackType<ListView<Item>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> ListView<Item> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, ListView<Item> item) throws E1, E2 { return visitor.visit(this, item); }
    }
    record Map<K extends CallbackItem, V extends CallbackItem>(CallbackType<K> key, CallbackType<V> value) implements CallbackType<MapView<K, V>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> MapView<K, V> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, MapView<K, V> item) throws E1, E2 { return visitor.visit(this, item); }
    }
    record Func<I extends CallbackItem, O extends CallbackItem>(CallbackType<I> param, CallbackType<O> returnType) implements CallbackType<FuncView<I, O>> {
        public static Func<CallbackItem, CallbackItem> ANY_TO_ANY = new Func<>(Any.INSTANCE, Any.INSTANCE);
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> FuncView<I, O> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, FuncView<I, O> item) throws E1, E2 { return visitor.visit(this, item); }
    }
    record Optional<Item extends CallbackItem>(CallbackType<Item> inner) implements CallbackType<CallbackItem.Optional<Item>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Optional<Item> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Optional<Item> item) throws E1, E2 { return visitor.visit(this, item); }
    }

    // Tuples. (These can be used as arguments to a Func)
    record Tuple2<A extends CallbackItem, B extends CallbackItem>(CallbackType<A> a, CallbackType<B> b) implements CallbackType<CallbackItem.Tuple2<A, B>>, Tuple<CallbackItem.Tuple2<A, B>> {
        @Override public int count() { return 2; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple2<A, B> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple2<A, B> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple2<A, B> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple2<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple2<A, B> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b())); }
    }
    record Tuple3<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c) implements CallbackType<CallbackItem.Tuple3<A, B, C>>, Tuple<CallbackItem.Tuple3<A, B, C>> {
        @Override public int count() { return 3; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple3<A, B, C> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple3<A, B, C> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple3<A, B, C> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple3<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple3<A, B, C> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c())); }
    }
    record Tuple4<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d) implements CallbackType<CallbackItem.Tuple4<A, B, C, D>>, Tuple<CallbackItem.Tuple4<A, B, C, D>> {
        @Override public int count() { return 4; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple4<A, B, C, D> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple4<A, B, C, D> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple4<A, B, C, D> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple4<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple4<A, B, C, D> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c()), d.fromItem(visitor, item == null ? null : item.d())); }
    }
    record Tuple5<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e) implements CallbackType<CallbackItem.Tuple5<A, B, C, D, E>>, Tuple<CallbackItem.Tuple5<A, B, C, D, E>> {
        @Override public int count() { return 5; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple5<A, B, C, D, E> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple5<A, B, C, D, E> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple5<A, B, C, D, E> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple5<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple5<A, B, C, D, E> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c()), d.fromItem(visitor, item == null ? null : item.d()), e.fromItem(visitor, item == null ? null : item.e())); }
    }
    record Tuple6<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f) implements CallbackType<CallbackItem.Tuple6<A, B, C, D, E, F>>, Tuple<CallbackItem.Tuple6<A, B, C, D, E, F>> {
        @Override public int count() { return 6; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple6<A, B, C, D, E, F> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple6<A, B, C, D, E, F> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple6<A, B, C, D, E, F> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple6<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple6<A, B, C, D, E, F> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c()), d.fromItem(visitor, item == null ? null : item.d()), e.fromItem(visitor, item == null ? null : item.e()), f.fromItem(visitor, item == null ? null : item.f())); }
    }
    record Tuple7<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f, CallbackType<G> g) implements CallbackType<CallbackItem.Tuple7<A, B, C, D, E, F, G>>, Tuple<CallbackItem.Tuple7<A, B, C, D, E, F, G>> {
        @Override public int count() { return 7; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple7<A, B, C, D, E, F, G> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple7<A, B, C, D, E, F, G> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple7<A, B, C, D, E, F, G> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple7<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5]), g.toItem(visitor, outside[6])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple7<A, B, C, D, E, F, G> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c()), d.fromItem(visitor, item == null ? null : item.d()), e.fromItem(visitor, item == null ? null : item.e()), f.fromItem(visitor, item == null ? null : item.f()), g.fromItem(visitor, item == null ? null : item.g())); }
    }
    record Tuple8<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f, CallbackType<G> g, CallbackType<H> h) implements CallbackType<CallbackItem.Tuple8<A, B, C, D, E, F, G, H>>, Tuple<CallbackItem.Tuple8<A, B, C, D, E, F, G, H>> {
        @Override public int count() { return 8; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> Outside fromItem(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item) throws E1, E2 { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple8<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5]), g.toItem(visitor, outside[6]), h.toItem(visitor, outside[7])); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> java.util.List<Outside> fromItems(FromItemVisitor<Outside, E1, E2> visitor, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item) throws E1, E2 { return java.util.List.of(a.fromItem(visitor, item == null ? null : item.a()), b.fromItem(visitor, item == null ? null : item.b()), c.fromItem(visitor, item == null ? null : item.c()), d.fromItem(visitor, item == null ? null : item.d()), e.fromItem(visitor, item == null ? null : item.e()), f.fromItem(visitor, item == null ? null : item.f()), g.fromItem(visitor, item == null ? null : item.g()), h.fromItem(visitor, item == null ? null : item.h())); }
    }

    // A Visitor that converts from a scripting object + Type to a CallbackItem
    // This is potentially fallible, since that outside object might not fit the proper type, so let it throw errors.
    interface ToItemVisitor<Outside, E1 extends Throwable, E2 extends Throwable> {
        // Primitive
        CallbackItem.Unit visit(Unit __, Outside outside) throws E1, E2;
        CallbackItem.Opaque visit(Opaque __, Outside outside) throws E1, E2;
        CallbackItem visit(Any __, Outside outside) throws E1, E2;
        CallbackItem.Bool visit(Bool __, Outside outside) throws E1, E2;
        CallbackItem.I32 visit(I32 __, Outside outside) throws E1, E2;
        CallbackItem.F32 visit(F32 __, Outside outside) throws E1, E2;
        CallbackItem.F64 visit(F64 __, Outside outside) throws E1, E2;
        StringView visit(Str __, Outside outside) throws E1, E2;
        // Figura items
        EntityView<?> visit(Entity __, Outside outside) throws E1, E2;

        // Generic
        <T extends CallbackItem> ListView<T> visit(List<T> list, Outside outside) throws E1, E2;
        <K extends CallbackItem, V extends CallbackItem> MapView<K, V> visit(Map<K, V> map, Outside outside) throws E1, E2;
        <I extends CallbackItem, O extends CallbackItem> FuncView<I, O> visit(Func<I, O> func, Outside outside) throws E1, E2;
        <T extends CallbackItem> CallbackItem.Optional<T> visit(Optional<T> optional, Outside outside) throws E1, E2;
        // Tuples
        <A extends CallbackItem, B extends CallbackItem> CallbackItem.Tuple2<A, B> visit(Tuple2<A, B> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> CallbackItem.Tuple3<A, B, C> visit(Tuple3<A, B, C> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> CallbackItem.Tuple4<A, B, C, D> visit(Tuple4<A, B, C, D> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> CallbackItem.Tuple5<A, B, C, D, E> visit(Tuple5<A, B, C, D, E> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> CallbackItem.Tuple6<A, B, C, D, E, F> visit(Tuple6<A, B, C, D, E, F> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> CallbackItem.Tuple7<A, B, C, D, E, F, G> visit(Tuple7<A, B, C, D, E, F, G> tuple, Outside outside) throws E1, E2;
        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> visit(Tuple8<A, B, C, D, E, F, G, H> tuple, Outside outside) throws E1, E2;
    }

    // A Visitor that converts from a CallbackItem + Type into an outside object.
    // Can be fallible, since constructing an outside object might be a problem (for example, OOM when converting to Lua)
    interface FromItemVisitor<Outside, E1 extends Throwable, E2 extends Throwable> {
        // Primitive
        Outside visit(Unit __, CallbackItem.Unit item) throws E1, E2;
        Outside visit(Opaque __, CallbackItem.Opaque item) throws E1, E2;
        Outside visit(Any __, CallbackItem item) throws E1, E2;
        Outside visit(Bool __, CallbackItem.Bool item) throws E1, E2;
        Outside visit(I32 __, CallbackItem.I32 item) throws E1, E2;
        Outside visit(F32 __, CallbackItem.F32 item) throws E1, E2;
        Outside visit(F64 __, CallbackItem.F64 item) throws E1, E2;
        Outside visit(Str __, StringView item) throws E1, E2;
        // Figura items
        Outside visit(Entity __, EntityView<?> item) throws E1, E2;
        // Generic
        <T extends CallbackItem> Outside visit(List<T> type, ListView<T> item) throws E1, E2;
        <K extends CallbackItem, V extends CallbackItem> Outside visit(Map<K, V> type, MapView<K, V> item) throws E1, E2;
        <I extends CallbackItem, O extends CallbackItem> Outside visit(Func<I, O> type, FuncView<I, O> item) throws E1, E2;
        <T extends CallbackItem> Outside visit(Optional<T> type, CallbackItem.Optional<T> item) throws E1, E2;
        // Tuples
        <TupleItem extends CallbackItem.Tuple> Outside visitTuple(Tuple<TupleItem> type, TupleItem item) throws E1, E2; // Allow a default impl for any tuple
        default <A extends CallbackItem, B extends CallbackItem> Outside visit(Tuple2<A, B> type, CallbackItem.Tuple2<A, B> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> Outside visit(Tuple3<A, B, C> type, CallbackItem.Tuple3<A, B, C> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> Outside visit(Tuple4<A, B, C, D> type, CallbackItem.Tuple4<A, B, C, D> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> Outside visit(Tuple5<A, B, C, D, E> type, CallbackItem.Tuple5<A, B, C, D, E> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> Outside visit(Tuple6<A, B, C, D, E, F> type, CallbackItem.Tuple6<A, B, C, D, E, F> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> Outside visit(Tuple7<A, B, C, D, E, F, G> type, CallbackItem.Tuple7<A, B, C, D, E, F, G> item) throws E1, E2 { return this.visitTuple(type, item); }
        default <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> Outside visit(Tuple8<A, B, C, D, E, F, G, H> type, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item) throws E1, E2 { return this.visitTuple(type, item); }
    }

    // Visitor for stringifying a type.
    // Just pass null as the item!
    class StringifyVisitor implements FromItemVisitor<String, RuntimeException, RuntimeException> {

        public static final StringifyVisitor INSTANCE = new StringifyVisitor();
        private StringifyVisitor() {}

        @Override public String visit(Unit __, CallbackItem.Unit ___) { return "()"; }
        @Override public String visit(Opaque __, CallbackItem.Opaque ___) { return "opaque"; }
        @Override public String visit(Any __, CallbackItem ___) { return "any"; }
        @Override public String visit(Bool __, CallbackItem.Bool ___) { return "bool"; }
        @Override public String visit(I32 __, CallbackItem.I32 ___) { return "i32"; }
        @Override public String visit(F32 __, CallbackItem.F32 ___) { return "f32"; }
        @Override public String visit(F64 __, CallbackItem.F64 ___) { return "f64"; }
        @Override public String visit(Str __, StringView ___) { return "string"; }

        @Override public String visit(Entity __, EntityView<?> ___) { return "entity"; }

        @Override public <T extends CallbackItem> String visit(List<T> type, ListView<T> ___) { return "[" + type.element.fromItem(this, null) + "]"; }
        @Override public <K extends CallbackItem, V extends CallbackItem> String visit(Map<K, V> type, MapView<K, V> ___) { return "{ " + type.key.fromItem(this, null) + " -> " + type.value.fromItem(this, null) + " }"; }
        @Override public <I extends CallbackItem, O extends CallbackItem> String visit(Func<I, O> type, FuncView<I, O> ___) { return "(" + type.param.fromItem(this, null) + " -> " + type.returnType.fromItem(this, null) + ")"; }
        @Override public <T extends CallbackItem> String visit(Optional<T> type, CallbackItem.Optional<T> ___) { return type.inner.fromItem(this, null) + "?"; }

        @Override
        public <TupleItem extends CallbackItem.Tuple> String visitTuple(Tuple<TupleItem> type, TupleItem item) throws RuntimeException {
            return "(" + String.join(", ", type.fromItems(this, item)) + ")";
        }
    }

    // Visitor for calculating the size of a type.
    // Just pass null as the item!
    class SizeVisitor implements FromItemVisitor<Integer, RuntimeException, RuntimeException> {
        public static final SizeVisitor INSTANCE = new SizeVisitor();
        private SizeVisitor() {}

        // Singletons are size 0
        @Override public Integer visit(Unit __, CallbackItem.Unit ___) { return 0; }
        @Override public Integer visit(Opaque __, CallbackItem.Opaque ___) { return 0; }
        @Override public Integer visit(Any __, CallbackItem ___) { return 0; }
        @Override public Integer visit(Bool __, CallbackItem.Bool ___) { return 0; }
        @Override public Integer visit(I32 __, CallbackItem.I32 ___) { return 0; }
        @Override public Integer visit(F32 __, CallbackItem.F32 ___) { return 0; }
        @Override public Integer visit(F64 __, CallbackItem.F64 ___) { return 0; }
        @Override public Integer visit(Str __, StringView ___) { return 0; }

        @Override public Integer visit(Entity __, EntityView<?> ___) { return 0; }

        @Override public <T extends CallbackItem> Integer visit(List<T> type, ListView<T> ___) {
            return AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE + type.element.fromItem(this, null);
        }
        @Override public <K extends CallbackItem, V extends CallbackItem> Integer visit(Map<K, V> type, MapView<K, V> ___) {
            return AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE * 2 + type.key.fromItem(this, null) + type.value.fromItem(this, null);
        }
        @Override public <I extends CallbackItem, O extends CallbackItem> Integer visit(Func<I, O> type, FuncView<I, O> ___) {
            return AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE * 2 + type.param.fromItem(this, null) + type.returnType.fromItem(this, null);
        }
        @Override public <T extends CallbackItem> Integer visit(Optional<T> type, CallbackItem.Optional<T> ___) {
            return AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE + type.inner.fromItem(this, null);
        }

        @Override
        public <TupleItem extends CallbackItem.Tuple> Integer visitTuple(Tuple<TupleItem> type, TupleItem item) throws RuntimeException {
            int size = AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE * type.count();
            for (int elemSize : type.fromItems(this, item))
                size += elemSize;
            return size;
        }
    }

    // Helper aliases? idk
    CallbackType<CallbackItem.Tuple2<CallbackItem.F32, CallbackItem.F32>> VEC2 = new Tuple2<>(F32.INSTANCE, F32.INSTANCE);
    CallbackType<CallbackItem.Tuple3<CallbackItem.F32, CallbackItem.F32, CallbackItem.F32>> VEC3 = new Tuple3<>(F32.INSTANCE, F32.INSTANCE, F32.INSTANCE);
    CallbackType<CallbackItem.Tuple4<CallbackItem.F32, CallbackItem.F32, CallbackItem.F32, CallbackItem.F32>> VEC4 = new Tuple4<>(F32.INSTANCE, F32.INSTANCE, F32.INSTANCE, F32.INSTANCE);

}
