package org.figuramc.figura_core.util.data_structures;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * A recursive tree data structure indexed by K, where leaves are V.
 * Useful for modeling filesystem directories.
 */
public class DataTree<K extends Comparable<K>, V> {

    private @Nullable DataTree<K, V> parent = null;
    public final TreeMap<K, DataTree<K, V>> nodeChildren = new TreeMap<>();
    public final TreeMap<K, V> leafChildren = new TreeMap<>();
    private int size = 0;

    public int size() {
        return size;
    }

    public V get(int i) {
        for (var nodeChild : nodeChildren.values()) {
            if (i < nodeChild.size)
                return nodeChild.get(i);
            i -= nodeChild.size;
        }
        return leafChildren.values().stream().skip(i).findFirst().orElseThrow(IndexOutOfBoundsException::new);
    }

    @Contract("_, _ -> this")
    public DataTree<K, V> addNode(K key, DataTree<K, V> node) {
        this.nodeChildren.put(key, node);
        if (node.parent != null) throw new IllegalArgumentException("Attempt to add DataTree node as child of multiple nodes");
        node.parent = this;
        addSize(node.size);
        return this;
    }

    @Contract("_, _ -> this")
    public DataTree<K, V> addLeaf(K key, V leaf) {
        this.leafChildren.put(key, leaf);
        addSize(1);
        return this;
    }

    private void addSize(int size) {
        DataTree<K, V> cur = this;
        while (cur != null) {
            cur.size += size;
            cur = cur.parent;
        }
    }

    public @NotNull Stream<Pair<ConsList<K>, V>> stream() {
        return Stream.concat(
                nodeChildren.entrySet().stream().flatMap(e ->
                        e.getValue().stream().map(p -> p.mapA(a -> ConsList.cons(e.getKey(), a)))),
                leafChildren.entrySet().stream().map(e -> new Pair<>(ConsList.cons(e.getKey(), ConsList.empty()), e.getValue()))
        );
    }
}
