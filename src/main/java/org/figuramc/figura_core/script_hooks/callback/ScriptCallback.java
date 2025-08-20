package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;

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
     * Get the Avatar which owns this Callback.
     * After calling a callback, the avatar which owns it might be errored.
     * If so, the caller should possibly handle this?
     */
    Avatar<?> getOwningAvatar();

    /**
     * Invoke the callback, applying translations, with the given arg. (Arg may be a tuple to simulate multiple args).
     * This should not throw errors like AvatarError, because those would be propagated to the CALLER!
     * This stage occurs AFTER the caller has already turned their items into CallbackItem. Any errors on the caller's
     * side would have already happened by this point. Any errors that occur inside the call are the fault of the CALLEE.
     * So these issues should be caught there, and they'll have their Avatar errored out.
     */
    O call(I arg);

}
