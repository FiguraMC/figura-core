package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.jetbrains.annotations.NotNull;

/**
 * A view of a ScriptCallback. It can be revoked to prevent memory hostage.
 */
public final class CallbackView<I extends CallbackItem, O extends CallbackItem> extends SimpleView<ScriptCallback<I, O>> implements CallbackItem, AutoCloseable {

    public CallbackView(@NotNull ScriptCallback<I, O> value) { super(value); }
    public CallbackView(@NotNull ScriptCallback<I, O> value, AbstractView parent) { super(value, parent); }

}
