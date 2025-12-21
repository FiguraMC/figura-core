package org.figuramc.figura_core.script_hooks.timing;

import org.figuramc.figura_core.avatars.Avatar;

/**
 * Receives messages and tracks the CPU time used by various avatars in various contexts.
 * This will be the primary method for users to determine "Which avatars are making my game lag?"
 *
 * The accountant continually receives reports, possibly from multiple threads, stating:
 * "At timestamp ___, avatar ___ ran for ___ nanoseconds, in ___ context."
 * Using this info, we can determine important trends.
 * We don't want to cause a memory leak from storing ALL of the diagnostic information, so don't hold on to it forever.
 */
public class CpuTimeAccountant {


    // The avatar is only to be used as an identity key.
    // We shouldn't access it in any way that cares about synchronization.
    // TODO: Should we care about which thread spent the time? Like if an avatar spends X time on one thread and Y time on another thread, it's less of an issue?
    public static void report(Avatar<?> avatar, long startTime, long elapsedTime, String context) {
        // For now, just println.
        System.out.println("At timestamp " + startTime + ", avatar " + avatar + " ran for " + elapsedTime + " nanos, in context: \"" + context + "\"");
    }

}
