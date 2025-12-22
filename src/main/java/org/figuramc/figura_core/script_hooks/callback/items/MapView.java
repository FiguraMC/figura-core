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

    @Override public synchronized void close() { this.keyType = null; this.valueType = null; super.close(); }

    public abstract int size(); // Number of entries in the map, or -1 if revoked
    public abstract @Nullable V get(K key); // Get value for key, or return null if revoked

}
