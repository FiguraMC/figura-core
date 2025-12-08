package org.figuramc.figura_core.avatars;

import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.figuramc.figura_core.util.functional.ThrowingBiFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A single component in an Avatar, good for keeping code organized.
 *
 * @param <Self> The implementor's type. Write the following to support types:
 *              public static final Type<ThisClass> TYPE = new Type<>(ThisClass.class, Types I depend on);
 */
public interface AvatarComponent<Self extends AvatarComponent<Self>> {

    // Each subclass of AvatarComponent must write the following line to support the ID system:
    // public static final Type<ThisClass> TYPE = new Type<>(ThisClass.class, Types I depend on);
    // Note that this falls apart if there's a dependency loop (A depends on B depends on A), so don't create such loops!

    // AvatarComponent.Type is enum-like for efficient access, and ability for other mods to add more.
    // The generic makes the Avatar.getComponent() method more convenient.
    final class Type<X extends AvatarComponent<X>> extends EnumLike {
        public final ThrowingBiFunction<Avatar<?>, AvatarModules, X, AvatarError> factory;
        // By passing the possible dependencies as arguments, we ensure they're initialized first, and therefore have smaller IDs.
        public Type(ThrowingBiFunction<Avatar<?>, AvatarModules, X, AvatarError> factory, Type<?>... possibleDependencies) {
            this.factory = factory;
            // If a value here is null, then you have a dependency cycle.
            if (!Arrays.stream(possibleDependencies).allMatch(dep -> dep != null && dep.id < this.id))
                throw new IllegalStateException();
        }
    }

    // Return true when this is completely ready to be initialized on the main thread.
    // Textures, for example, perform uploading asynchronously,
    // so they should only return true once all textures have been uploaded properly.
    default boolean isReady() { return true; }

    // Run on Avatar cleanup. Should eventually destroy any native resources that won't be GC'ed and prevent a memory leak.
    default void destroy() { }

    // Runs when the Avatar is ticked.
    // Dependencies will always run before this, as declared in createId().
    default void tick() throws AvatarError { }

}
