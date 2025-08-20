package org.figuramc.figura_core.util;

import org.figuramc.figura_core.util.functional.ThrowingBiFunction;
import org.figuramc.figura_core.util.functional.ThrowingFunction;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MapUtils {

    public static <K, V1, V2, E extends Throwable> HashMap<K, V2> mapValues(Map<K, V1> map, ThrowingFunction<V1, V2, E> func) throws E {
        return mapValues(map, func, HashMap::new);
    }

    public static <M extends Map<K, V2>, K, V1, V2, E extends Throwable> M mapValues(Map<K, V1> map, ThrowingFunction<V1, V2, E> func, Supplier<M> mapSupplier) throws E {
        M result = mapSupplier.get();
        for (Map.Entry<K, V1> entry : map.entrySet())
            result.put(entry.getKey(), func.apply(entry.getValue()));
        return result;
    }

    public static <M extends Map<K, V2>, K, V1, V2, E extends Throwable> M mapValues(Map<K, V1> map, ThrowingBiFunction<K, V1, V2, E> func, Supplier<M> mapSupplier) throws E {
        M result = mapSupplier.get();
        for (Map.Entry<K, V1> entry : map.entrySet())
            result.put(entry.getKey(), func.apply(entry.getKey(), entry.getValue()));
        return result;
    }


    public static <K, V1, V2, E extends Throwable> HashMap<K, V2> mapValues(Map<K, V1> map, ThrowingBiFunction<K, V1, V2, E> func) throws E {
        HashMap<K, V2> result = new HashMap<>();
        for (Map.Entry<K, V1> entry : map.entrySet())
            result.put(entry.getKey(), func.apply(entry.getKey(), entry.getValue()));
        return result;
    }

    public static <K1, K2, V, E extends Throwable> HashMap<K2, V> mapKeys(Map<K1, V> map, ThrowingFunction<K1, K2, E> func) throws E {
        HashMap<K2, V> result = new HashMap<>();
        for (Map.Entry<K1, V> entry : map.entrySet())
            result.put(func.apply(entry.getKey()), entry.getValue());
        return result;
    }

    public static <K, V, T, E extends Throwable> List<T> mapEntries(Map<K, V> map, ThrowingBiFunction<K, V, T, E> func) throws E {
        List<T> result = new ArrayList<>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet())
            result.add(func.apply(entry.getKey(), entry.getValue()));
        return result;
    }


    public static <K, V> LinkedHashMap<K, List<V>> merge(Iterable<Map<K, V>> maps) {
        LinkedHashMap<K, List<V>> result = new LinkedHashMap<>();
        for (Map<K, V> map : maps)
            for (Map.Entry<K, V> entry : map.entrySet())
                result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
        return result;
    }
    public static <K, V> LinkedHashMap<K, V> mergeAssertUnique(Iterable<Map<K, V>> maps) {
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map<K, V> map : maps) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (result.containsKey(entry.getKey()))
                    throw new IllegalArgumentException("mergeAssertUnique expects unique keys when merging maps");
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }


}
