package org.figuramc.figura_core.util.enumlike;

import org.figuramc.figura_core.util.functional.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// Map for objects using the integer ID system
// No hashmap lookup, uses direct array
// Values cannot be null
// Expects method calls to not violate generic rules
public class IdMap<K extends EnumLike, V> implements Map<K, V> {

    private int size = 0;
    private final List<? extends K> allKeys; // Shared reference to global list.
    private final List<@Nullable V> values;

    // Empty map
    public IdMap(Class<K> kClass) {
        this(kClass, __ -> null);
    }

    // Initialize with a value for every key
    public <E extends Throwable> IdMap(Class<? super K> kClass, ThrowingFunction<K, @Nullable V, E> initial) throws E {
        allKeys = EnumLike.values(kClass);
        values = new ArrayList<>(allKeys.size());
        for (K key : allKeys) {
            V val = initial.apply(key);
            if (val != null) size++;
            values.add(val);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return values.get(((K) key).id) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return Arrays.asList(values).contains(value);
    }

    @Override
    public V get(Object key) {
        return values.get(((K) key).id);
    }

    @Override
    public @Nullable V put(K key, V value) {
        int id = key.id;
        V oldVal = values.set(id, value);
        if (value != null && oldVal == null) size++;
        else if (value == null && oldVal != null) size--;
        return oldVal;
    }

    @Override
    public V remove(Object key) {
        int id = ((K) key).id;
        V oldVal = values.set(id, null);
        if (oldVal != null) size--;
        return oldVal;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        Collections.fill(values, null);
        this.size = 0;
    }

    @Override
    public @NotNull Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        return values; // Just return the backing collection directly
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    private class KeySet extends AbstractIdMapBackingSet<K, K> {
        @Override K toKey(K item) { return item; }
        @Override K fromKey(K key) { return key; }
    }
    private class EntrySet extends AbstractIdMapBackingSet<IdEntry, Entry<K, V>> {
        @Override K toKey(IdEntry item) { return item.key; }
        @Override IdEntry fromKey(K key) { return new IdEntry(key); }
    }
    private class IdEntry implements Entry<K, V> {
        private final K key;
        public IdEntry(K key) { this.key = key; }

        @Override public K getKey() { return key; }
        @Override public V getValue() { return IdMap.this.get(key); }
        @Override public V setValue(V value) { return IdMap.this.put(key, value); }
    }

    // T is either K or Entry<K, V>
    // We extract common functionality.
    private abstract class AbstractIdMapBackingSet<T extends TSuper, TSuper> extends AbstractSet<TSuper> {

        // Operations to convert to/from key, this is the only difference between EntrySet and KeySet.
        abstract K toKey(T item);
        abstract T fromKey(K key);

        @Override
        public int size() {
            return IdMap.this.size();
        }

        @Override
        public boolean contains(Object o) {
            return IdMap.this.containsKey(toKey((T) o));
        }

        @Override
        public @NotNull Iterator<TSuper> iterator() {
            return new Iterator<>() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    findNextValue();
                    return i != values.size();
                }

                @Override
                public T next() {
                    findNextValue();
                    T res = fromKey(allKeys.get(i));
                    i++;
                    return res;
                }

                @Override
                public void remove() {
                    values.set(i - 1, null);
                }

                // Increment i until it's pointing at the next non-null value.
                // If it's already pointing at a non-null value, does nothing.
                private void findNextValue() {
                    while (i < values.size()) {
                        if (values.get(i) != null) break;
                        i++;
                    }
                }
            };
        }

        @Override
        public boolean add(TSuper o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return IdMap.this.remove(toKey((T) o)) != null;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for (Object o : c)
                if (!contains(o))
                    return false;
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends TSuper> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            boolean changed = false;
            Iterator<TSuper> iter = iterator();
            while (iter.hasNext()) {
                if (!c.contains(iter.next())) {
                    iter.remove();
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public void clear() {
            IdMap.this.clear();
        }
    }

}
