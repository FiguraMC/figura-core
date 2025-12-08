package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.jetbrains.annotations.Nullable;

/**
 * A view of a List. It supports get/set and size.
 * It can be frozen to prevent mutation, and revoked to prevent all access
 * (and allow GC'ing the internals, preventing memory hostage situations)
 */
public abstract non-sealed class ListView<T extends CallbackItem> implements CallbackItem, AutoCloseable {

    public final CallbackType<T> callbackType;

    public ListView(CallbackType<T> callbackType) {
        this.callbackType = callbackType;
    }

    private static final byte NOT_REVOKED = 0;
    private static final byte FROZEN = 1;
    private static final byte REVOKED = 2;
    private byte revokeState = NOT_REVOKED;

    @Override public synchronized void close() { revokeState = REVOKED; }
    public synchronized void freeze() { if (revokeState == NOT_REVOKED) revokeState = FROZEN; }

    // DO NOT use isRevoked/isFrozenOrRevoked as a check for whether an operation is safe to run!
    // Another thread might revoke the item after you call this!
    // Use it only as a means for diagnosing a sentinel-return in the actual methods!
    public boolean isRevoked() { return revokeState == REVOKED; }
    public boolean isFrozenOrRevoked() { return revokeState >= FROZEN; }

    public abstract /* synchronized */ int length(); // Number of items in the list, or -1 if revoked

    // Set the item at the given index and return true on success.
    // Return false if the view is frozen or revoked.
    // Attempting to get at an index >= length, or index < 0, should throw an error.
    public abstract /* synchronized */ boolean set(int index, T item);

    // Get item at index, or return null if revoked.
    // Attempting to get at an index >= length, or index < 0, should throw an error.
    // Also note that this function is responsible for conversion from callee's lang -> T, which may fail.
    // If conversion fails, this is the fault of the CALLEE, so error out the callee.
    public abstract /* synchronized */ @Nullable T get(int index);


}
