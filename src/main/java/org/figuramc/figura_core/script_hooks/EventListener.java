package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.data_structures.Mutable;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_core.util.functional.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
    private final ArrayList<Pair<ScriptCallback<Args, Ret>, CallbackHandle>> callbacks = new ArrayList<>(); // TODO: Track this with allocation tracker
    private final ArrayDeque<QueuedInvocation<Args>> queuedInvocations = new ArrayDeque<>(); // TODO: Track this with allocation tracker, and add a max capacity to prevent OOM attacks

    private final Set<CallbackHandle> queuedCallbackRemoval = new HashSet<>(); // Callbacks marked for removal
    private int nesting; // Amount of nesting

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
    public CallbackHandle registerCallback(ScriptCallback<Args, Ret> callback) {
        assert callback.getOwningAvatar() == owningAvatar; // Debug assert we're upholding the contract
        CallbackHandle handle = new CallbackHandle(this);
        this.callbacks.add(new Pair<>(callback, handle));
        return handle;
    }

    // Adds the given callback to the removal queue.
    public void removeCallback(CallbackHandle handle) {
        queuedCallbackRemoval.add(handle);
    }

    // Special handle referencing this; it can be called to remove itself
    public static class CallbackHandle {
        public final EventListener<?, ?> listener;
        private CallbackHandle(EventListener<?, ?> listener) {
            this.listener = listener;
        }
        public void removeThis() {
            listener.removeCallback(this);
        }
    }

    // -------- IMMEDIATE INVOCATION (Owning avatar only) -------- //

    // Contains common iteration and removal logic
    // Return true to break out of the loop and return true from the whole invocation.
    // If the predicate never returns true, this function returns false.
    private synchronized <E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> boolean invokeImpl(TriThrowingFunction<ScriptCallback<Args, Ret>, Boolean, E1, E2, E3> predicate) throws E1, E2, E3 {
        boolean removedAny = false;
        nesting++;
        try {
            for (int i = 0; i < callbacks.size(); i++) {
                var pair = callbacks.get(i);
                if (pair == null) continue;
                if (queuedCallbackRemoval.remove(pair.b())) {
                    removedAny = true;
                    callbacks.set(i, null);
                } else {
                    if (predicate.apply(pair.a()))
                        return true;
                }
            }
        } finally {
            nesting--;
            if (removedAny && nesting == 0)
                callbacks.removeIf(Objects::isNull);
        }
        return false;
    }

    // Invoke the event listener with the given args, ignoring returns.
    public void invoke(Args args) throws AvatarError {
        invokeImpl(callback -> {
            callback.call(args);
            return false;
        });
    }

    // Invoke the event listener, canceling if a result passes the predicate
    // Returns true if any function canceled, or false if none canceled
    public <E1 extends Throwable, E2 extends Throwable> boolean invokeCanceling(Args args, BiThrowingFunction<Ret, Boolean, E1, E2> shouldCancel) throws AvatarError, E1, E2 {
        return this.<E1, E2, AvatarError>invokeImpl(callback -> shouldCancel.apply(callback.call(args)));
    }

    // Invoke the event listener with the given args, returning a list of results.
    public List<Ret> invokeToList(Args args) throws AvatarError {
        List<Ret> res = new ArrayList<>();
        invokeImpl(callback -> {
            res.add(callback.call(args));
            return false;
        });
        return res;
    }

    // Invoke the event listener in a chained manner: initialArg (-> event -> chainer)* -> return
    public <E1 extends Throwable, E2 extends Throwable> Args invokeChained(Args initialArg, BiThrowingFunction<Ret, Args, E1, E2> chainer) throws AvatarError, E1, E2 {
        Mutable<Args> cur = new Mutable<>(initialArg);
        this.<E1, E2, AvatarError>invokeImpl(callback -> {
            cur.value = chainer.apply(callback.call(cur.value));
            return false;
        });
        return cur.value;
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
