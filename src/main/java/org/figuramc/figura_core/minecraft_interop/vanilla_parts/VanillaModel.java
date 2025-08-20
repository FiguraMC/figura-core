package org.figuramc.figura_core.minecraft_interop.vanilla_parts;

import org.figuramc.figura_core.util.functional.ThrowingBiConsumer;
import org.jetbrains.annotations.Nullable;

/**
 * A model is just a bucket of parts with names.
 * There are helper subclasses to ensure you implement the model properly (i.e. as the Figura spec expects)
 * for a given entity, but these classes are just suggestions;
 * the only thing you actually need to implement is the VanillaModel interface.
 */
public interface VanillaModel {
    // Run the given consumer on every name/part pair.
    // The consumer is expected to always be run on parents before their children.
    <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E;

    // Helper to return as the default, it just has no parts in it.
    VanillaModel EMPTY = new VanillaModel() {
        @Override
        public <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E {}
    };

}
