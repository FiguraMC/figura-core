package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Each Avatar is given a built-in EventListener per Event.
 * Scripts are also allowed to dynamically create EventListeners. (This is TODO)
 * An EventListener is just a type plus a list of callbacks, which can be invoked.
 * If a callback returns true, it is removed from the list.
 *
 * The Java side maintains a collection of built-in EventListeners, which it can invoke when an event occurs.
 */
public class EventListener<Args extends CallbackItem, Ret extends CallbackItem> {

    public final Avatar<?> owningAvatar; // The avatar which owns this event listener. Only Callbacks owned by the same avatar should be added to it.
    public final CallbackType.Func<Args, Ret> funcType; // Type for callbacks
    private final ArrayList<ScriptCallback<Args, Ret>> callbacks = new ArrayList<>();

    // Requires static param types on creation
    public EventListener(CallbackType.Func<Args, Ret> funcType, Avatar<?> owningAvatar) {
        this.funcType = funcType;
        this.owningAvatar = owningAvatar;
    }

    // Only allow appending to the list at the end.
    // This is because of the potential for a callback to register additional callbacks.
    // The callback MUST be owned by the same avatar as this EventListener!
    public void registerCallback(ScriptCallback<Args, Ret> callback) {
        this.callbacks.add(callback);
    }

    // Invoke the event listener with the given args, ignoring returns.
    public void invoke(Args args) {
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            if (owningAvatar.isErrored()) break;
            callback.call(args);
        }
    }

    // Invoke the event listener with the given args, returning a list of results.
    public List<Ret> invokeFor(Args args) {
        List<Ret> res = new ArrayList<>();
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            if (owningAvatar.isErrored()) break;
            Ret result = callback.call(args);
            if (result != null) res.add(result);
        }
        return res;
    }

    public Args invokeChained(Args initialArg, Function<Ret, Args> chainer) {
        Args cur = initialArg;
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            if (owningAvatar.isErrored()) break;
            Ret result = callback.call(cur);
            if (result == null) break; // The avatar is errored if we get null
            cur = chainer.apply(result);
        }
        return cur;
    }

}
