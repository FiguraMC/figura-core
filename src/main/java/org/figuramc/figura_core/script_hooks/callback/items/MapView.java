package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.jetbrains.annotations.Nullable;

public abstract non-sealed class MapView<K extends CallbackItem, V extends CallbackItem> extends AbstractView implements CallbackItem {

    protected CallbackType<K> keyType;
    protected CallbackType<V> valueType;

    public MapView(CallbackType<K> keyType, CallbackType<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    // Returns null if revoked
    public @Nullable CallbackType<K> keyType() { return keyType; }
    public @Nullable CallbackType<V> valueType() { return valueType; }

    @Override public synchronized void close() { this.keyType = null; this.valueType = null; super.close(); }

    public abstract /* synchronized */ int size(); // Number of entries in the map, or -1 if revoked
    public abstract /* synchronized */ @Nullable V get(K key); // Get value for key, or return null if revoked
    public abstract /* synchronized */ @Nullable Iterable<K> keys(); // Get iterable of keys

}
