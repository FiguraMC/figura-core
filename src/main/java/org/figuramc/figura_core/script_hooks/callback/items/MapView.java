package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract non-sealed class MapView<K extends CallbackItem, V extends CallbackItem> implements CallbackItem, AutoCloseable {

    public CallbackType<K> keyType;
    public CallbackType<V> valueType;

    public MapView(CallbackType<K> keyType, CallbackType<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    private static final byte NOT_REVOKED = 0;
    private static final byte FROZEN = 1;
    private static final byte REVOKED = 2;

    private byte revokeState = NOT_REVOKED;
    @Override public synchronized void close() { revokeState = REVOKED; keyType = null; valueType = null; }
    public synchronized void freeze() { if (revokeState == NOT_REVOKED) revokeState = FROZEN; }

    // DO NOT use isRevoked/isFrozenOrRevoked as a check for whether an operation is safe to run!
    // Another thread might revoke the item after you call this!
    // Use it only as a means for diagnosing a sentinel-return in the actual methods!
    public boolean isRevoked() { return revokeState == REVOKED; }
    public boolean isFrozenOrRevoked() { return revokeState >= FROZEN; } // Frozen or revoked

    public abstract /* synchronized */ int size(); // Number of entries in the map, or -1 if revoked
    public abstract /* synchronized */ boolean put(K key, V value); // Put the key/value pair in the map and return true if successful, or return false if frozen/revoked
    public abstract /* synchronized */ boolean remove(K key); // Remove the key's entry from the map and return true if successful, or return false if frozen/revoked
    public abstract /* synchronized */ @Nullable V get(K key); // Get value for key, or return null if revoked
    public abstract /* synchronized */ @Nullable Iterable<K> keys(); // Get an iterable of all keys, or null if revoked.


}
