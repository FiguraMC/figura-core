package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    private final ArrayList<Pair<ScriptCallback<Args, Ret>, Long>> callbacks = new ArrayList<>(); // TODO: Track this with allocation tracker
    private final ArrayDeque<QueuedInvocation<Args>> queuedInvocations = new ArrayDeque<>(); // TODO: Track this with allocation tracker, and add a max capacity to prevent OOM attacks

    private final Set<Long> queuedCallbackRemoval = new LinkedHashSet<>(); // Callbacks marked for removal
    public static long eventCallbackId = 0; // An incrementing integer to identify callbacks

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
    public long registerCallback(ScriptCallback<Args, Ret> callback) {
        assert callback.getOwningAvatar() == owningAvatar; // Debug assert we're upholding the contract
        eventCallbackId++;
        this.callbacks.add(new Pair<>(callback, eventCallbackId));
        return eventCallbackId;
    }

    // Adds the given callback to the removal queue.
    public void removeCallback(long callbackId) {
        queuedCallbackRemoval.add(callbackId);
    }

    // -------- IMMEDIATE INVOCATION (Owning avatar only) -------- //

    // Invoke the event listener with the given args, ignoring returns.
    public void invoke(Args args) throws AvatarError {
        boolean removedAny = false;
        for (int i = 0; i < callbacks.size(); i++) {
            var pair = callbacks.get(i);
            // Check if callback is slated for removal
            if (queuedCallbackRemoval.contains(pair.b())) {
                removedAny = true;
                callbacks.set(i, null);
                queuedCallbackRemoval.remove(pair.b());
            } else {
                pair.a().call(args);
            }
        }
        // Clear any remaining nulls if we removed callbacks.
        if (removedAny) callbacks.removeIf(Objects::isNull);
    }

    // Invoke the event listener, canceling if a result passes the predicate
    // Returns true if any function canceled, or false if none canceled
    public <E1 extends Throwable, E2 extends Throwable> boolean invokeCanceling(Args args, BiThrowingFunction<Ret, Boolean, E1, E2> shouldCancel) throws AvatarError, E1, E2 {
        boolean removedAny = false;
        for (int i = 0; i < callbacks.size(); i++) {
            var pair = callbacks.get(i);
            // Check if callback is slated for removal
            if (queuedCallbackRemoval.contains(pair.b())) {
                removedAny = true;
                callbacks.set(i, null);
                queuedCallbackRemoval.remove(pair.b());
            } else {
                Ret result = pair.a().call(args);
                if (shouldCancel.apply(result)) return true;
            }
        }
        // Clear any remaining nulls if we removed callbacks.
        if (removedAny) callbacks.removeIf(Objects::isNull);

        return false;
    }

    // Invoke the event listener with the given args, returning a list of results.
    public List<Ret> invokeToList(Args args) throws AvatarError {
        List<Ret> res = new ArrayList<>();

        boolean removedAny = false;
        for (int i = 0; i < callbacks.size(); i++) {
            var pair = callbacks.get(i);
            // Check if callback is slated for removal
            if (queuedCallbackRemoval.contains(pair.b())) {
                removedAny = true;
                callbacks.set(i, null);
                queuedCallbackRemoval.remove(pair.b());
            } else {
                res.add(pair.a().call(args));
            }
        }
        // Clear any remaining nulls if we removed callbacks.
        if (removedAny) callbacks.removeIf(Objects::isNull);

        return res;
    }

    // Invoke the event listener in a chained manner: initialArg (-> event -> chainer)* -> return
    public <E1 extends Throwable, E2 extends Throwable> Args invokeChained(Args initialArg, BiThrowingFunction<Ret, Args, E1, E2> chainer) throws AvatarError, E1, E2 {
        Args cur = initialArg;

        boolean removedAny = false;
        for (int i = 0; i < callbacks.size(); i++) {
            var pair = callbacks.get(i);
            // Check if callback is slated for removal
            if (queuedCallbackRemoval.contains(pair.b())) {
                removedAny = true;
                callbacks.set(i, null);
                queuedCallbackRemoval.remove(pair.b());
            } else {
                Ret result = pair.a().call(cur);
                cur = chainer.apply(result);
            }
        }
        // Clear any remaining nulls if we removed callbacks.
        if (removedAny) callbacks.removeIf(Objects::isNull);

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
