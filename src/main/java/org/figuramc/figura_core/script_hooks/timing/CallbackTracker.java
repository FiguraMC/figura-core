package org.figuramc.figura_core.script_hooks.timing;

import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;

/**
 * Keeps track of how script callbacks are invoking each other, handling timeouts and interruptions for them.
 */
public class CallbackTracker {



    // Push when we're about to invoke a callback
    // Pass the callback we're about to invoke as well as a deadline
    public static void pushCall(ScriptCallback<?, ?> callback, long deadline) {

    }

}
