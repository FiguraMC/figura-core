package org.figuramc.figura_core.script_hooks.flags;

import org.figuramc.figura_core.util.functional.BiThrowingRunnable;
import org.figuramc.figura_core.util.functional.BiThrowingSupplier;

// A BehaviorFlag is a "global" (thread-local) value that can be enabled/disabled,
// affecting operations of other code in this thread.
// Used as an opportunity to avoid deep-drilling niche/rarely-used method parameters.
public class BehaviorFlag {

    private final ThreadLocal<Boolean> enabled = ThreadLocal.withInitial(() -> false);
    public boolean enabled() { return this.enabled.get(); }

    // Run the given task with this flag enabled on this thread.
    public <E1 extends Throwable, E2 extends Throwable> void use(BiThrowingRunnable<E1, E2> task) throws E1, E2 {
        this.enabled.set(true);
        try {
            task.run();
        } finally {
            this.enabled.set(false);
        }
    }
    public <R, E1 extends Throwable, E2 extends Throwable> R useFor(BiThrowingSupplier<R, E1, E2> task) throws E1, E2 {
        this.enabled.set(true);
        try {
            return task.get();
        } finally {
            this.enabled.set(false);
        }
    }

}
