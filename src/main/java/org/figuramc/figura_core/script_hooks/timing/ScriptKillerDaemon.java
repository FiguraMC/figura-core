package org.figuramc.figura_core.script_hooks.timing;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a background thread that kills tasks when they go on too long.
 */
public class ScriptKillerDaemon extends Thread {

    // The number of millis to Thread.sleep() for between checks.
    // A value of 100 means that a task might possibly go un-killed for about 100ms longer than it should.
    private static final long RESOLUTION_MILLIS = 50;

    // The set of currently-running tasks.
    private final ConcurrentHashMap<Task, Boolean> currentlyRunning = new ConcurrentHashMap<>();

    // Singleton
    public static final ScriptKillerDaemon INSTANCE = new ScriptKillerDaemon();
    private ScriptKillerDaemon() {
        this.setDaemon(true);
        this.start();
    }

    // Begin a task
    public void startTask(Task task) {
        currentlyRunning.put(task, Boolean.TRUE);
    }

    // Return true if the task was successfully finished.
    // False if it was already finished because the budget timed out.
    public boolean endTask(Task task) {
        return currentlyRunning.remove(task) != null;
    }

    @Override
    public void run() {
        // Infinite loop
        while (true) {
            // Wait some amount of time to not waste CPU
            try { Thread.sleep(RESOLUTION_MILLIS); } catch (InterruptedException ignored) {}
            // Check each task in the currently-running set, removing ones which are killed
            currentlyRunning.keySet().removeIf(Task::check);
        }
    }

    // Contains:
    // - A callback to kill something if time expires
    // - Start time of the task in nanos
    // - Time budget in nanos, after which the callback should be killed
    // Many of these are inserted and removed from the set of currently running tasks constantly.
    public record Task(Runnable killer, long start, long budget) {

        public Task(Runnable killer, long budget) { this(killer, System.nanoTime(), budget); }

        public boolean check() {
            if (System.nanoTime() - start > budget) {
                killer.run();
                return true;
            }
            return false;
        }
    }

}
