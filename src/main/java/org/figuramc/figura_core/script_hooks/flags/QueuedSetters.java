package org.figuramc.figura_core.script_hooks.flags;

import java.util.ArrayList;

/**
 * Implementation of Queued Setters in script for thread safety stuff.
 * While the flag is enabled, certain setters which are relevant to rendering will be queued up instead of immediately executed.
 * The queue can be flushed at a later time by client code.
 */
public class QueuedSetters {

    // Flag object for enabling/disabling this behavior
    public static final BehaviorFlag FLAG = new BehaviorFlag();

    private static final Object LOCK = new Object();
    private static ArrayList<Runnable> TASKS = new ArrayList<>();

    // Take the tasks, emptying the list of queued setters
    public static ArrayList<Runnable> getTasks() {
        synchronized (LOCK) {
            ArrayList<Runnable> result = TASKS;
            TASKS = new ArrayList<>();
            return result;
        }
    }

    // If setters are queued, queue it. Otherwise, execute immediately.
    // Hopefully jit inlining can make this not too horrible...?
    public static void handle(Runnable task) {
        if (FLAG.enabled()) register(task);
        else task.run();
    }

    // Queue a task
    public static void register(Runnable task) {
        synchronized (LOCK) {
            TASKS.add(task);
        }
    }

}
