package org.figuramc.figura_core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class SetUtils {

    public static <T> Set<T> union(Collection<Set<T>> sets) {
        if (sets.isEmpty()) return Set.of();
        Set<T> result = new HashSet<>();
        for (Set<T> set : sets) result.addAll(set);
        return result;
    }
    @SafeVarargs
    public static <T> Set<T> union(Set<T>... sets) { return union(List.of(sets)); }

    public static <T> Set<T> intersection(Collection<Set<T>> sets) {
        if (sets.isEmpty()) return Set.of();
        var iter = sets.iterator();
        Set<T> result = new HashSet<>(iter.next());
        while (iter.hasNext()) result.retainAll(iter.next());
        return result;
    }
    @SafeVarargs
    public static <T> Set<T> intersection(Set<T>... sets) { return intersection(List.of(sets)); }

    // Return A - B
    public static <T> Set<T> without(Set<T> a, Set<T> b) {
        HashSet<T> result = new HashSet<>(a);
        a.removeAll(b);
        return result;
    }


}
