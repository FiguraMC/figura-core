package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;

/**
 * A view of a List. It supports get/set and size.
 * It can be frozen to prevent mutation, and revoked to prevent all access
 * (and allow GC'ing the internals, preventing memory hostage situations)
 */
public abstract non-sealed class ListView<T extends CallbackItem> implements CallbackItem {

    public final CallbackType<T> callbackType;

    public ListView(CallbackType<T> callbackType) {
        this.callbackType = callbackType;
    }

    private static final byte NOT_REVOKED = 0;
    private static final byte FROZEN = 1;
    private static final byte REVOKED = 2;

    private byte revokeState = NOT_REVOKED;

    public void revoke() { revokeState = REVOKED; }
    public boolean isRevoked() { return revokeState == REVOKED; }
    public void freeze() { if (revokeState == NOT_REVOKED) revokeState = FROZEN; }
    public boolean isFrozenOrRevoked() { return revokeState >= FROZEN; }

    public abstract int length(); // Number of items in the list

    // Set item at index, or throw an error if frozen/revoked.
    // Attempting to get at an index >= length, or index < 0, is also an error.
    public abstract void set(int index, T item);

    // Get item at index, or throw an error if revoked.
    // Attempting to get at an index >= length, or index < 0, is also an error.
    // Also note that this function is responsible for conversion from callee's lang -> T, which may fail.
    // If it's revoked, this is the fault of the CALLER, so THROW an error back to the caller.
    // If conversion fails, this is the fault of the CALLEE, so error out the callee.
    public abstract T get(int index);


}
