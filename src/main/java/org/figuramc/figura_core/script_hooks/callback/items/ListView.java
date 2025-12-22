package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.jetbrains.annotations.Nullable;

/**
 * A view of a List. It supports get/set and size.
 * It can be frozen to prevent mutation, and revoked to prevent all access
 * (and allow GC'ing the internals, preventing memory hostage situations)
 */
public abstract non-sealed class ListView<T extends CallbackItem> extends AbstractView implements CallbackItem {

    protected CallbackType<T> elementType;

    public ListView(CallbackType<T> elementType) {
        this.elementType = elementType;
    }

    // Returns null if revoked
    public @Nullable CallbackType<T> elementType() { return elementType; }

    @Override public synchronized void close() { this.elementType = null; super.close(); }

    public abstract /* synchronized */ int length(); // Number of items in the list, or -1 if revoked

    // Get item at index, or return null if revoked.
    // Attempting to get at an index >= length, or index < 0, should throw an error.
    // Also note that this function is responsible for conversion from callee's lang -> T, which may fail.
    // If conversion fails, this is the fault of the CALLEE, so error out the callee.
    public abstract /* synchronized */ @Nullable T get(int index);


}
