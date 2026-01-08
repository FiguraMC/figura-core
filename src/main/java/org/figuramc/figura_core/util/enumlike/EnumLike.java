package org.figuramc.figura_core.util.enumlike;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class where each instance has a unique integer ID.
 * This means we can implement efficient data structures.
 * It's similar to how an enum can use EnumMap, but we don't want to use enums
 * directly because addons can't construct additional instances.
 */
public abstract class EnumLike {

    // The ID of this item.
    public final int id;

    // All EnumLike have a name for serialization purposes
    public final String name;

    // Static incrementing IDs based on subclass, and arrays of items based on subclass
    // If the key is present, but the ID is null, that indicates this class has been FROZEN.
    private static final Map<Class<?>, @Nullable AtomicInteger> NEXT_IDS = new HashMap<>();
    private static final Map<Class<?>, List<EnumLike>> ALL_VALUES = new HashMap<>();
    private static final Map<Class<?>, Map<String, EnumLike>> VALUES_BY_NAME = new HashMap<>(); // Maps uppercase and lower case

    private static boolean isFrozen(Class<?> subclass) {
        return NEXT_IDS.containsKey(subclass) && NEXT_IDS.get(subclass) == null;
    }

    public static void freeze(Class<? extends EnumLike> subclass) {
        if (subclass.getSuperclass() != EnumLike.class)
            throw new IllegalArgumentException("Argument to EnumLike.freeze() must be direct subclass of EnumLike");
        NEXT_IDS.put((subclass), null);
    }

    public EnumLike(String name) {
        // Compute the direct subclass
        Class<?> subclass = getClass();
        while (subclass.getSuperclass() != EnumLike.class)
            subclass = subclass.getSuperclass();
        // If it's frozen, error out
        if (isFrozen(subclass))
            throw new IllegalStateException("Attempt to create instance for class \"" + subclass.getName() + "\" after it was frozen! Ensure all instances are set up before calling freeze()! Likely a problem with static initializers not being run in the expected order.");
        // Generate ID based on the subclass (we know it's not mapped to null)
        this.id = NEXT_IDS.computeIfAbsent(subclass, __ -> new AtomicInteger(0)).getAndIncrement();
        // Append this to the list of values
        ALL_VALUES.computeIfAbsent(subclass, __ -> new ArrayList<>()).add(this);
        // Add this to the names map
        this.name = name;
        insertName(name, VALUES_BY_NAME.computeIfAbsent(subclass, __ -> new HashMap<>()));
    }

    private void insertName(String name, Map<String, EnumLike> map) {
        String upper = name.toUpperCase();
        String lower = name.toLowerCase();
        EnumLike oldUpper = map.put(upper, this);
        EnumLike oldLower = map.put(lower, this);
        if (oldUpper != null || oldLower != null)
            throw new IllegalArgumentException("Multiple EnumLike instances with the same name \"" + name + "\"");
    }

    // Get all values for the given class. Its direct superclass must be EnumLike.
    @SuppressWarnings("unchecked")
    public static <T extends EnumLike> List<? extends T> values(Class<? super T> clazz) {
        if (clazz.getSuperclass() != EnumLike.class)
            throw new IllegalArgumentException("Argument to EnumLike.values() must be direct subclass of EnumLike");
        if (!isFrozen(clazz))
            throw new IllegalArgumentException("Cannot call EnumLike.values() before calling EnumLike.freeze()!");
        return (List<? extends T>) ALL_VALUES.getOrDefault(clazz, List.of());
    }

    // Get an instance by name for the given class. Its direct superclass must be EnumLike.
    // If there is no element with that name, returns null.
    // Name can either be in SCREAMING_SNAKE_CASE or snake_case (not a_MiX_oF_bOtH)
    @SuppressWarnings("unchecked")
    public static <T extends EnumLike> @Nullable T byName(Class<? super T> clazz, String name) {
        if (clazz.getSuperclass() != EnumLike.class)
            throw new IllegalArgumentException("Argument to EnumLike.byName() must be direct subclass of EnumLike");
        if (!isFrozen(clazz))
            throw new IllegalArgumentException("Cannot call EnumLike.byName() before calling EnumLike.freeze()!");
        Map<String, ? extends T> map = (Map<String, ? extends T>) VALUES_BY_NAME.get(clazz);
        if (map == null) return null;
        return map.get(name);
    }

    public static <T extends EnumLike> int count(Class<? super T> clazz) {
        return values(clazz).size();
    }

    // Hashcode impl
    @Override
    public int hashCode() {
        return id;
    }

    // Usually prefer .name unless debugging
    @Override
    public String toString() {
        // Compute the direct subclass
        Class<?> subclass = getClass();
        while (subclass.getSuperclass() != EnumLike.class)
            subclass = subclass.getSuperclass();
        return subclass.getSimpleName() + "[" + this.id + " = \"" + this.name + "\"]";
    }
}
