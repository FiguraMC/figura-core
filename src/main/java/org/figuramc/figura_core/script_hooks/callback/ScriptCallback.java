package org.figuramc.figura_core.script_hooks.callback;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.jetbrains.annotations.NotNull;

/**
 * A callback within a ScriptRuntime that can be sent outside of that runtime.
 * However, it CANNOT be sent between avatars!
 *
 * Avatars should not directly invoke ScriptCallback defined in another avatar!
 * If users intend to pass functions to another avatar, use an EventListener instead.
 */
public interface ScriptCallback<I extends CallbackItem, O extends CallbackItem> {

    /**
     * Get the type of this callback
     */
    CallbackType.Func<I, O> type();

    /**
     * Get the owning Avatar. Only use this for simple reference comparisons,
     * since it's not wrapped in a View.
     */
    Avatar<?> getOwningAvatar();

    /**
     * Invoke the callback, applying translations, with the given arg. (Arg may be a tuple to simulate multiple args).
     *
     * Be careful with this function:
     * There should NEVER be a time when two Avatars' code is on the call stack at the same time!
     * If an Avatar wants to call another Avatar's function, it has to do it asynchronously through
     * something like EventListener.
     *
     * Note that the args have been already converted into statically typed I.
     * Any AvatarError thrown by this method is therefore the fault of the callee,
     * and the callee should be the one punished accordingly.
     *
     * @param arg The statically typed argument to the callback
     */
    @NotNull O call(I arg) throws AvatarError;

}
