package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.script_hooks.callback.items.*;
import org.figuramc.memory_tracker.AllocationTracker;

import java.util.function.IntFunction;

// Types that can be sent through callbacks; statically checked and converted.
// Correlated through the generic to the corresponding CallbackItem.
public sealed interface CallbackType<T extends CallbackItem> {

    <Outside, E1 extends Throwable, E2 extends Throwable> T toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2;
    <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, T item);


    default String stringify() { return fromItem(StringifyVisitor.INSTANCE, null); }
    default int getSize() { return fromItem(SizeVisitor.INSTANCE, null); }

    // Max supported tuple size
    int MAX_TUPLE_SIZE = 8;

    sealed interface Tuple<TupleItem extends CallbackItem> extends CallbackType<TupleItem> {
        int count(); // Number of items in the tuple
        <Outside, E1 extends Throwable, E2 extends Throwable> TupleItem toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] items) throws E1, E2;
        <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, TupleItem items, IntFunction<Outside[]> arraySupplier);
    }

    // "Void" / "Nothing" / null
    final class Unit implements CallbackType<CallbackItem.Unit>, Tuple<CallbackItem.Unit> {
        public static final Unit INSTANCE = new Unit(); private Unit() {}
        @Override public int count() { return 0; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Unit toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Unit item) { return visitor.visit(this, item); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Unit toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] items) { return CallbackItem.Unit.INSTANCE; }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Unit item, IntFunction<Outside[]> arraySupplier) { return arraySupplier.apply(0); }
    }

    // Primitives
    final class Any implements CallbackType<CallbackItem> {
        public static final Any INSTANCE = new Any(); private Any() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem item) { return visitor.visit(this, item); }
    }
    final class Bool implements CallbackType<CallbackItem.Bool> {
        public static final Bool INSTANCE = new Bool(); private Bool() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Bool toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Bool item) { return visitor.visit(this, item); }
    }
    final class F32 implements CallbackType<CallbackItem.F32> {
        public static final F32 INSTANCE = new F32(); private F32() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.F32 toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.F32 item) { return visitor.visit(this, item); }
    }
    final class F64 implements CallbackType<CallbackItem.F64> {
        public static final F64 INSTANCE = new F64(); private F64() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.F64 toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.F64 item) { return visitor.visit(this, item); }
    }
    final class Str implements CallbackType<StringView> {
        public static final Str INSTANCE = new Str(); private Str() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> StringView toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, StringView item) { return visitor.visit(this, item); }
    }

    // Figura items
    final class Entity implements CallbackType<EntityView<?>> {
        public static final Entity INSTANCE = new Entity(); private Entity() {}
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> EntityView<?> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, EntityView<?> item) { return visitor.visit(this, item); }
    }

    // Objects
//    final class FiguraPart implements CallbackType { public static final FiguraPart INSTANCE = new FiguraPart(); private FiguraPart() {} }

    // Generic types
    record List<Item extends CallbackItem>(CallbackType<Item> element) implements CallbackType<ListView<Item>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> ListView<Item> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { return visitor.visit(this, outside); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, ListView<Item> item) { return visitor.visit(this, item); }
    }
    record Map<K extends CallbackItem, V extends CallbackItem>(CallbackType<K> key, CallbackType<V> value) implements CallbackType<MapView<K, V>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> MapView<K, V> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, MapView<K, V> item) { throw new UnsupportedOperationException("TODO"); }
    }
    record Func<I extends CallbackItem, O extends CallbackItem>(CallbackType<I> param, CallbackType<O> returnType) implements CallbackType<FuncView<I, O>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> FuncView<I, O> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, FuncView<I, O> item) { throw new UnsupportedOperationException("TODO"); }
    }
    record Optional<Item extends CallbackItem>(CallbackType<Item> inner) implements CallbackType<CallbackItem.Optional<Item>> {
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Optional<Item> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Optional<Item> item) { throw new UnsupportedOperationException("TODO"); }
    }

    // Tuples. (These can be used as arguments to a Func)
    record Tuple2<A extends CallbackItem, B extends CallbackItem>(CallbackType<A> a, CallbackType<B> b) implements CallbackType<CallbackItem.Tuple2<A, B>>, Tuple<CallbackItem.Tuple2<A, B>> {
        @Override public int count() { return 2; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple2<A, B> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple2<A, B> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple2<A, B> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple2<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple2<A, B> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b())).toArray(arraySupplier); }
    }
    record Tuple3<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c) implements CallbackType<CallbackItem.Tuple3<A, B, C>>, Tuple<CallbackItem.Tuple3<A, B, C>> {
        @Override public int count() { return 3; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple3<A, B, C> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple3<A, B, C> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple3<A, B, C> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple3<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple3<A, B, C> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c())).toArray(arraySupplier); }
    }
    record Tuple4<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d) implements CallbackType<CallbackItem.Tuple4<A, B, C, D>>, Tuple<CallbackItem.Tuple4<A, B, C, D>> {
        @Override public int count() { return 4; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple4<A, B, C, D> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple4<A, B, C, D> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple4<A, B, C, D> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple4<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple4<A, B, C, D> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c()), d.fromItem(visitor, item.d())).toArray(arraySupplier); }
    }
    record Tuple5<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e) implements CallbackType<CallbackItem.Tuple5<A, B, C, D, E>>, Tuple<CallbackItem.Tuple5<A, B, C, D, E>> {
        @Override public int count() { return 5; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple5<A, B, C, D, E> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple5<A, B, C, D, E> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple5<A, B, C, D, E> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple5<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple5<A, B, C, D, E> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c()), d.fromItem(visitor, item.d()), e.fromItem(visitor, item.e())).toArray(arraySupplier); }
    }
    record Tuple6<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f) implements CallbackType<CallbackItem.Tuple6<A, B, C, D, E, F>>, Tuple<CallbackItem.Tuple6<A, B, C, D, E, F>> {
        @Override public int count() { return 6; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple6<A, B, C, D, E, F> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple6<A, B, C, D, E, F> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple6<A, B, C, D, E, F> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple6<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple6<A, B, C, D, E, F> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c()), d.fromItem(visitor, item.d()), e.fromItem(visitor, item.e()), f.fromItem(visitor, item.f())).toArray(arraySupplier); }
    }
    record Tuple7<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f, CallbackType<G> g) implements CallbackType<CallbackItem.Tuple7<A, B, C, D, E, F, G>>, Tuple<CallbackItem.Tuple7<A, B, C, D, E, F, G>> {
        @Override public int count() { return 7; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple7<A, B, C, D, E, F, G> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple7<A, B, C, D, E, F, G> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple7<A, B, C, D, E, F, G> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple7<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5]), g.toItem(visitor, outside[6])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple7<A, B, C, D, E, F, G> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c()), d.fromItem(visitor, item.d()), e.fromItem(visitor, item.e()), f.fromItem(visitor, item.f()), g.fromItem(visitor, item.g())).toArray(arraySupplier); }
    }
    record Tuple8<A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem>(CallbackType<A> a, CallbackType<B> b, CallbackType<C> c, CallbackType<D> d, CallbackType<E> e, CallbackType<F> f, CallbackType<G> g, CallbackType<H> h) implements CallbackType<CallbackItem.Tuple8<A, B, C, D, E, F, G, H>>, Tuple<CallbackItem.Tuple8<A, B, C, D, E, F, G, H>> {
        @Override public int count() { return 8; }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> toItem(ToItemVisitor<Outside, E1, E2> visitor, Outside outside) throws E1, E2 { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside> Outside fromItem(FromItemVisitor<Outside> visitor, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item) { throw new UnsupportedOperationException("TODO"); }
        @Override public <Outside, E1 extends Throwable, E2 extends Throwable> CallbackItem.Tuple8<A, B, C, D, E, F, G, H> toItems(ToItemVisitor<Outside, E1, E2> visitor, Outside[] outside) throws E1, E2 { return new CallbackItem.Tuple8<>(a.toItem(visitor, outside[0]), b.toItem(visitor, outside[1]), c.toItem(visitor, outside[2]), d.toItem(visitor, outside[3]), e.toItem(visitor, outside[4]), f.toItem(visitor, outside[5]), g.toItem(visitor, outside[6]), h.toItem(visitor, outside[7])); }
        @Override public <Outside> Outside[] fromItems(FromItemVisitor<Outside> visitor, CallbackItem.Tuple8<A, B, C, D, E, F, G, H> item, IntFunction<Outside[]> arraySupplier) { return java.util.List.of(a.fromItem(visitor, item.a()), b.fromItem(visitor, item.b()), c.fromItem(visitor, item.c()), d.fromItem(visitor, item.d()), e.fromItem(visitor, item.e()), f.fromItem(visitor, item.f()), g.fromItem(visitor, item.g()), h.fromItem(visitor, item.h())).toArray(arraySupplier); }
    }

    // A Visitor that converts from a scripting object + Type to a CallbackItem
    // This is potentially fallible, since that outside object might not fit the proper type, so let it throw errors.
    interface ToItemVisitor<Outside, E1 extends Throwable, E2 extends Throwable> {
        // Primitive
        CallbackItem.Unit visit(Unit __, Outside outside) throws E1, E2;
        CallbackItem visit(Any __, Outside outside) throws E1, E2;
        CallbackItem.Bool visit(Bool __, Outside outside) throws E1, E2;
        CallbackItem.F32 visit(F32 __, Outside outside) throws E1, E2;
        CallbackItem.F64 visit(F64 __, Outside outside) throws E1, E2;
        StringView visit(Str __, Outside outside) throws E1, E2;
        // Figura items
        EntityView<?> visit(Entity __, Outside outside) throws E1, E2;

        // Generic
        <T extends CallbackItem> ListView<T> visit(List<T> list, Outside outside) throws E1, E2;
//        <K extends CallbackItem, V extends CallbackItem> R visit(Map<K, V> map);
//        <I extends CallbackItem, O extends CallbackItem> R visit(Func<I, O> func);
//        <T extends CallbackItem> R visit(Optional<T> optional);
//        // Tuples
//        <A extends CallbackItem, B extends CallbackItem> R visit(Tuple2<A, B> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem> R visit(Tuple3<A, B, C> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem> R visit(Tuple4<A, B, C, D> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem> R visit(Tuple5<A, B, C, D, E> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem> R visit(Tuple6<A, B, C, D, E, F> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem> R visit(Tuple7<A, B, C, D, E, F, G> tuple);
//        <A extends CallbackItem, B extends CallbackItem, C extends CallbackItem, D extends CallbackItem, E extends CallbackItem, F extends CallbackItem, G extends CallbackItem, H extends CallbackItem> R visit(Tuple8<A, B, C, D, E, F, G, H> tuple);
    }

    // A Visitor that converts from a CallbackItem + Type into an outside object.
    interface FromItemVisitor<Outside> {
        // Primitive
        Outside visit(Unit __, CallbackItem.Unit item);
        Outside visit(Any __, CallbackItem item);
        Outside visit(Bool __, CallbackItem.Bool item);
        Outside visit(F32 __, CallbackItem.F32 item);
        Outside visit(F64 __, CallbackItem.F64 item);
        Outside visit(Str __, StringView item);
        // Figura items
        Outside visit(Entity __, EntityView<?> item);
        // Generic
        <T extends CallbackItem> Outside visit(List<T> list, ListView<T> item);
    }

    // Visitor for stringifying a type.
    // Just pass null as the item!
    class StringifyVisitor implements FromItemVisitor<String> {

        public static final StringifyVisitor INSTANCE = new StringifyVisitor();
        private StringifyVisitor() {}

        @Override public String visit(Unit __, CallbackItem.Unit ___) { return "()"; }
        @Override public String visit(Any __, CallbackItem ___) { return "any"; }
        @Override public String visit(Bool __, CallbackItem.Bool ___) { return "bool"; }
        @Override public String visit(F32 __, CallbackItem.F32 ___) { return "f32"; }
        @Override public String visit(F64 __, CallbackItem.F64 ___) { return "f64"; }
        @Override public String visit(Str __, StringView ___) { return "string"; }

        @Override public String visit(Entity __, EntityView<?> ___) { return "entity"; }

        @Override public <T extends CallbackItem> String visit(List<T> list, ListView<T> ___) { return "[" + list.element.fromItem(this, null) + "]"; }
    }

    // Visitor for calculating the size of a type.
    // Just pass null as the item!
    class SizeVisitor implements FromItemVisitor<Integer> {
        public static final SizeVisitor INSTANCE = new SizeVisitor();
        private SizeVisitor() {}

        // Singletons are size 0
        @Override public Integer visit(Unit __, CallbackItem.Unit ___) { return 0; }
        @Override public Integer visit(Any __, CallbackItem ___) { return 0; }
        @Override public Integer visit(Bool __, CallbackItem.Bool ___) { return 0; }
        @Override public Integer visit(F32 __, CallbackItem.F32 ___) { return 0; }
        @Override public Integer visit(F64 __, CallbackItem.F64 ___) { return 0; }
        @Override public Integer visit(Str __, StringView ___) { return 0; }

        @Override public Integer visit(Entity __, EntityView<?> ___) { return 0; }

        @Override public <T extends CallbackItem> Integer visit(List<T> list, ListView<T> ___) {
            return AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE + list.element.fromItem(this, null);
        }
    }


    // Helper aliases? idk
    CallbackType<CallbackItem.Tuple2<CallbackItem.F32, CallbackItem.F32>> VEC2 = new Tuple2<>(F32.INSTANCE, F32.INSTANCE);
    CallbackType<CallbackItem.Tuple3<CallbackItem.F32, CallbackItem.F32, CallbackItem.F32>> VEC3 = new Tuple3<>(F32.INSTANCE, F32.INSTANCE, F32.INSTANCE);
    CallbackType<CallbackItem.Tuple4<CallbackItem.F32, CallbackItem.F32, CallbackItem.F32, CallbackItem.F32>> VEC4 = new Tuple4<>(F32.INSTANCE, F32.INSTANCE, F32.INSTANCE, F32.INSTANCE);

}
