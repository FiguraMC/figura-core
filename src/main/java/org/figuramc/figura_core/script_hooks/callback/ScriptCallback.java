package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.jetbrains.annotations.Nullable;

/**
 * A callback within a ScriptRuntime that can be sent outside of that runtime.
 * <p>
 * Many things need to be considered to keep inter-avatar callbacks safe; one of which is Views.
 * In order to prevent various problems with passing values between Avatars, Views exist.
 * Without a view system, arbitrarily-sized types which are passed between Avatars can create issues with memory ownership.
 * - Avatar A allocates a List
 * - Avatar A passes the List to Avatar B's callback
 * - Avatar B saves it in a global variable
 * - The List cannot be garbage collected even if A stops referring to it, so A's memory is permanently held hostage by B.
 * Situations like this necessitate something like a View, to make it possible to defend against these memory-hostage attacks.
 */
public interface ScriptCallback<I extends CallbackItem, O extends CallbackItem> {

    /**
     * Get the type of this callback
     */
    CallbackType.Func<I, O> type();

    /**
     * Get a view of the Avatar which owns this Callback.
     * After calling a callback, the avatar which owns it might be errored.
     * If so, the caller should possibly handle this?
     */
    AvatarView<?> getOwningAvatar();

    /**
     * Invoke the callback, applying translations, with the given arg. (Arg may be a tuple to simulate multiple args).
     * This should not throw errors like AvatarError, because those would be propagated to the CALLER!
     * This stage occurs AFTER the caller has already turned their items into CallbackItem. Any errors on the caller's
     * side would have already happened by this point. Any errors that occur inside the call are the fault of the CALLEE.
     * So these issues should be caught there, and they'll have their Avatar errored out.
     * If the callee errored, this function will return null.
     *
     * @param caller The callback which is calling this one.
     *               If no other callback is calling it (it's called from Figura itself) then pass null.
     * @param timeout The length of the timeout to set, in nanoseconds.
     *                If the call takes longer than that, then the function should return null early, restoring
     *                control to the caller.
     * @param arg The statically typed argument to the callback
     */
    @Nullable O call(@Nullable ScriptCallback<?,?> caller, long timeout, I arg);

}
