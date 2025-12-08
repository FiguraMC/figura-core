package org.figuramc.figura_core.script_hooks.callback.items;

import org.jetbrains.annotations.Nullable;

public abstract non-sealed class MapView<K extends CallbackItem, V extends CallbackItem> implements CallbackItem, AutoCloseable {

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
    public boolean isFrozenOrRevoked() { return revokeState >= FROZEN; } // Frozen or revoked

    public abstract int size(); // Number of entries in the map, or -1 if revoked
    public abstract boolean put(K key, V value); // Put the key/value pair in the map and return true if successful, or return false if frozen/revoked
    public abstract boolean remove(K key); // Remove the key's entry from the map and return true if successful, or return false if frozen/revoked
    public abstract @Nullable V get(K key); // Get value for key, or return null if revoked

}
