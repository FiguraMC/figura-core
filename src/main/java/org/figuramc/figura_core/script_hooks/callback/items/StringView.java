package org.figuramc.figura_core.script_hooks.callback.items;

import org.jetbrains.annotations.Nullable;

// Strings are immutable, so there is no "frozen" status like with a List.
public abstract non-sealed class StringView implements CallbackItem, AutoCloseable {

    protected boolean isRevoked = false;
    @Override public synchronized void close() { isRevoked = true; }

    // DO NOT use this as a check for whether an operation is safe to run!
    // Another thread might revoke the item after you call this!
    // Use it only as a means for diagnosing a sentinel-return in the actual methods!
    public boolean isRevoked() { return isRevoked; }

    public abstract /* synchronized */ int length(); // Length of the string, or -1 if revoked
    public abstract /* synchronized */ @Nullable String copy(); // Copy out the string, or null if revoked
}
