package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Each Avatar is given a built-in EventListener per Event.
 * Scripts are also allowed to dynamically create EventListeners. (This is TODO)
 * An EventListener consists of a type plus a list of callbacks, which can be invoked.
 *
 * EventListener are also used as the closest approximation for "function calls" between avatars.
 * It implements this using a message queueing system. Each EventListener has an associated "owning avatar".
 * When the EventListener is called by a non-owning avatar, it is not invoked immediately; instead the arguments are queued.
 * Later, the owning avatar may flush the queue and run the registered callbacks.
 *
 * TODO: Implement rate limits and/or maximum queue size to prevent OOM attacks on the owner by queueing many calls
 *
 * The Java side maintains a collection of built-in EventListeners, which it can invoke when an event occurs.
 */
public class EventListener<Args extends CallbackItem, Ret extends CallbackItem> {

    public final Avatar<?> owningAvatar; // No need for a View since this is only used for reference comparisons
    public final CallbackType.Func<Args, Ret> funcType; // Type for callbacks
    private final ArrayList<ScriptCallback<Args, Ret>> callbacks = new ArrayList<>(); // TODO: Track this with allocation tracker
    private final ArrayDeque<QueuedInvocation<Args>> queuedInvocations = new ArrayDeque<>(); // TODO: Track this with allocation tracker, and add a max capacity to prevent OOM attacks

    public record QueuedInvocation<Args extends CallbackItem>(Args args, AvatarView<?> callingAvatar) {}

    // Requires static param types on creation
    public EventListener(Avatar<?> owningAvatar, CallbackType.Func<Args, Ret> funcType) {
        this.owningAvatar = owningAvatar;
        this.funcType = funcType;
    }

    // -------- ADDING/REMOVING FUNCTIONS (Owning avatar only) -------- //

    // Only allow appending to the list at the end.
    // This is because of the potential for a callback to register additional callbacks.
    // The callback MUST be defined by the same avatar as this EventListener!
    public void registerCallback(ScriptCallback<Args, Ret> callback) {
        assert callback.getOwningAvatar() == owningAvatar; // Debug assert we're upholding the contract
        this.callbacks.add(callback);
    }

    // TODO: function to remove a callback. Have registerCallback return a handle which is then passed to removeCallback?

    // -------- IMMEDIATE INVOCATION (Owning avatar only) -------- //

    // Invoke the event listener with the given args, ignoring returns.
    public void invoke(Args args) throws AvatarError {
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            callback.call(args);
        }
    }

    // Invoke the event listener, canceling if a result passes the predicate
    // Returns true if any function canceled, or false if none canceled
    public <E1 extends Throwable, E2 extends Throwable> boolean invokeCanceling(Args args, BiThrowingFunction<Ret, Boolean, E1, E2> shouldCancel) throws AvatarError, E1, E2 {
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            Ret result = callback.call(args);
            if (shouldCancel.apply(result)) return true;
        }
        return false;
    }

    // Invoke the event listener with the given args, returning a list of results.
    public List<Ret> invokeToList(Args args) throws AvatarError {
        List<Ret> res = new ArrayList<>();
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            res.add(callback.call(args));
        }
        return res;
    }

    // Invoke the event listener in a chained manner: initialArg (-> event -> chainer)* -> return
    public <E1 extends Throwable, E2 extends Throwable> Args invokeChained(Args initialArg, BiThrowingFunction<Ret, Args, E1, E2> chainer) throws AvatarError, E1, E2 {
        Args cur = initialArg;
        for (ScriptCallback<Args, Ret> callback : callbacks) {
            Ret result = callback.call(cur);
            cur = chainer.apply(result);
        }
        return cur;
    }

    // -------- QUEUEING -------- //

    // Queue an invocation from another avatar
    public void queueInvocation(Args args, AvatarView<?> caller) {
        queuedInvocations.push(new QueuedInvocation<>(args, caller));
    }

    // Check if there are any queued invocations
    public boolean isQueueEmpty() {
        return queuedInvocations.isEmpty();
    }

    // Poll an invocation from another avatar, or null if none are queued
    public @Nullable QueuedInvocation<Args> pollInvocation() {
        return queuedInvocations.poll();
    }


}
