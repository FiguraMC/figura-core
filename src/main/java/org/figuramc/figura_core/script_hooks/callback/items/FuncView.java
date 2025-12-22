package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.jetbrains.annotations.Nullable;

/**
 * Revocable view wrapping a ScriptCallback that can be revoked
 */
public final class FuncView<I extends CallbackItem, O extends CallbackItem> extends SimpleView<ScriptCallback<I, O>> implements CallbackItem {

    public FuncView(ScriptCallback<I, O> callback) { super(callback); }
    public FuncView(ScriptCallback<I, O> callback, AbstractView parent) { super(callback, parent); }

}
