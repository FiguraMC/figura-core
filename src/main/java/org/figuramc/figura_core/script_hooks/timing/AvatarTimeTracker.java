package org.figuramc.figura_core.script_hooks.timing;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.components.AvatarProfiling;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_core.util.functional.ThrowingRunnable;
import org.figuramc.figura_core.util.functional.ThrowingSupplier;
import org.jetbrains.annotations.Nullable;

/**
 * Helper methods for tracking time used by Avatars.
 * Uses ScriptKillerDaemon for implementation
 */
public class AvatarTimeTracker {

    // Singleton
    private static final AvatarTimeTracker INSTANCE = new AvatarTimeTracker();
    public static AvatarTimeTracker getInstance() {
        return INSTANCE;
    }

    // Return a runnable to kill an avatar with a time budget error if the budget runs out
    private static Runnable killAvatar(Avatar<?> avatar, long budgetNanos) {
        return () -> avatar.error(new AvatarError(FiguraException.LITERAL, "Overran budget of " + (budgetNanos / 1_000_000) + " ms (TODO translate)"));
    }

    // Run the given runnable, with the given Avatar, with the given budget.
    // If the runnable throws an AvatarError, the Avatar will be errored.
    // If the timer exceeds the budget in nanoseconds, the Avatar will be errored. (Note that the timing is approximate)
    public void runTimed(Avatar<?> avatar, ProfilingCategory category, long budgetNanos, ThrowingRunnable<AvatarError> runnable) {
        ScriptKillerDaemon.Task task = new ScriptKillerDaemon.Task(killAvatar(avatar, budgetNanos), budgetNanos);
        long start = System.nanoTime();
        ScriptKillerDaemon.INSTANCE.startTask(task);
        try {
            runnable.run();
        } catch (AvatarError error) {
            avatar.error(error);
        } finally {
            ScriptKillerDaemon.INSTANCE.endTask(task);
            // If the avatar has a profiler component, notify it of the elapsed time
            AvatarProfiling profiling = avatar.getComponent(AvatarProfiling.TYPE);
            if (profiling != null) profiling.recordTime(category, System.nanoTime() - start);
        }
    }

    // Variant of runTimed returning a value, or null if an error is thrown/avatar runs out of time
    public <R> @Nullable R runTimedFor(Avatar<?> avatar, ProfilingCategory category, long budgetNanos, ThrowingSupplier<R, AvatarError> supplier) {
        ScriptKillerDaemon.Task task = new ScriptKillerDaemon.Task(killAvatar(avatar, budgetNanos), budgetNanos);
        long start = System.nanoTime();
        ScriptKillerDaemon.INSTANCE.startTask(task);
        R result = null;
        try {
            result = supplier.get();
        } catch (AvatarError error) {
            avatar.error(error);
        } finally {
            if (!ScriptKillerDaemon.INSTANCE.endTask(task))
                result = null;
            // If the avatar has a profiler component, notify it of the elapsed time
            AvatarProfiling profiling = avatar.getComponent(AvatarProfiling.TYPE);
            if (profiling != null) profiling.recordTime(category, System.nanoTime() - start);
        }
        return result;
    }

}
