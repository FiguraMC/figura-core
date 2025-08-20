package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;

import java.util.ArrayList;

/**
 * Each Avatar is given a built-in EventListener per Event.
 * Scripts are also allowed to dynamically create EventListeners. (This is TODO)
 * An EventListener is just a type plus a list of callbacks, which can be invoked.
 * If a callback returns true, it is removed from the list.
 *
 * The Java side maintains a collection of built-in EventListeners, which it can invoke when an event occurs.
 */
public class EventListener<Args extends CallbackItem> {

    public final CallbackType.Func<Args, CallbackItem.Bool> funcType; // Type for callbacks
    private final ArrayList<ScriptCallback<Args, CallbackItem.Bool>> callbacks = new ArrayList<>();

    // Requires static param types on creation
    public EventListener(CallbackType.Func<Args, CallbackItem.Bool> funcType) {
        this.funcType = funcType;
    }

    // Only allow appending to the list at the end.
    // This is because of the potential for a callback to register additional callbacks.
    public void registerCallback(ScriptCallback<Args, CallbackItem.Bool> callback) {
        this.callbacks.add(callback);
    }

    // Invoke the event listener with the given args.
    public void invoke(Args args) {
        // Any callback returning true will be removed.
        for (int i = 0; i < callbacks.size(); i++) {
            ScriptCallback<Args, CallbackItem.Bool> callback = callbacks.get(i);
            boolean remove = callback.getOwningAvatar().isErrored() || callback.call(args).value();
            if (remove) { callbacks.remove(i); i--; }
        }
    }
}
