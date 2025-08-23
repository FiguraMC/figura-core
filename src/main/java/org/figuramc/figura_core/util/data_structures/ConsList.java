package org.figuramc.figura_core.util.data_structures;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public sealed abstract class ConsList<T> extends AbstractList<T> {

    @SuppressWarnings("unchecked")
    public static <T> ConsList<T> empty() { return (ConsList<T>) Empty.INSTANCE; }

    public static <T> ConsList<T> cons(T item, ConsList<T> rest) {
        return new Cons<>(item, rest);
    }

    // Create a ConsList from the given list
    public static <T> ConsList<T> of(List<T> list) {
        ConsList<T> cur = empty();
        for (var item : list.reversed())
            cur = new Cons<>(item, cur);
        return cur;
    }

    public static final class Empty extends ConsList {
        private static final Empty INSTANCE = new Empty();
        private Empty() {}
        @Override public int size() { return 0; }
        @Override public Object get(int index) { throw new IndexOutOfBoundsException(); }
        @Override public @NotNull Iterator<Object> iterator() { return Collections.emptyIterator(); }
    }

    public static final class Cons<T> extends ConsList<T> {

        public final T item;
        public final ConsList<T> rest;
        private final int size;

        public Cons(T item, ConsList<T> rest) {
            this.item = item;
            this.rest = rest;
            this.size = rest.size() + 1;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public T get(int index) {
            Cons<T> cur = this;
            while (index > 0) {
                if (rest instanceof Cons<T> consRest) {
                    cur = consRest;
                    index--;
                } else throw new IndexOutOfBoundsException();
            }
            return cur.item;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {
                private ConsList<T> cur = Cons.this;

                @Override
                public boolean hasNext() {
                    return cur != Empty.INSTANCE;
                }

                @Override
                public T next() {
                    T item = ((Cons<T>) cur).item;
                    cur = ((Cons<T>) cur).rest;
                    return item;
                }
            };
        }
    }


}
