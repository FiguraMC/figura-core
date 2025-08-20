package org.figuramc.figura_core.script_hooks.callback.items;

/**
 *
 */
public abstract non-sealed class MapView<K extends CallbackItem, V extends CallbackItem> implements CallbackItem {

    private static final byte NOT_REVOKED = 0;
    private static final byte FROZEN = 1;
    private static final byte REVOKED = 2;

    private byte revokeState = NOT_REVOKED;

    public void revoke() { revokeState = REVOKED; }
    public boolean isRevoked() { return revokeState == REVOKED; }
    public void freeze() { if (revokeState == NOT_REVOKED) revokeState = FROZEN; }
    public boolean isFrozenOrRevoked() { return revokeState >= FROZEN; } // Frozen or revoked

    public abstract int size(); // Number of entries in the map
    public abstract void put(K key, V value); // Put the key/value pair in the map, or error if frozen/revoked
    public abstract void remove(K key); // Remove the key's entry from the map, or error if frozen/revoked
    public abstract V get(K key); // Get value for key, or error if revoked

}
