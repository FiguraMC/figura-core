package org.figuramc.figura_core.script_hooks.callback.items;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple View of a generic item T.
 * Has a getter to fetch the T, or null if revoked.
 * Simple views are used for Minecraft resources, and so make use of the parent system.
 *
 * We use multiple subclasses of SimpleView so that they can be different types
 * and we can switch on them nicely at runtime by looking at the Class
 */
public class SimpleView<T> extends AbstractView {

    private @Nullable T value;

    public SimpleView(@NotNull T value) {
        this.value = value;
    }
    public SimpleView(@NotNull T value, AbstractView parent) {
        this.value = value;
        registerToParent(parent);
    }

    @Override public boolean isRevoked() { return value == null; }
    @Override public synchronized void close() { value = null; super.close(); }

    // Return the value, or null if revoked.
    public synchronized @Nullable T getValue() { return value; }

}
