package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.jetbrains.annotations.Nullable;

/**
 * Revocable view wrapping a ScriptCallback that can be revoked
 */
public final class FuncView<I extends CallbackItem, O extends CallbackItem> implements CallbackItem, AutoCloseable {

    private ScriptCallback<I, O> callback;

    public FuncView(ScriptCallback<I, O> callback) {
        this.callback = callback;
    }

    @Override public synchronized void close() { callback = null; }

    // DO NOT use isRevoked as a check for whether an operation is safe to run!
    // Another thread might revoke the item after you call this!
    // Use it only as a means for diagnosing a sentinel-return in the actual methods!
    public boolean isRevoked() { return callback == null; }

    // Get the callback, or null if revoked.
    public synchronized @Nullable ScriptCallback<I, O> getCallback() { return callback; }

}
