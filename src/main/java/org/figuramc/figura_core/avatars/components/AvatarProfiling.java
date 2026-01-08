package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.script_hooks.timing.ProfilingCategory;

import java.util.HashMap;
import java.util.Map;

/**
 * Keep data about time spent by this avatar
 * TODO maybe extend to also contain debugging functionality? Or separate component?
 */
public class AvatarProfiling implements AvatarComponent<AvatarProfiling> {

    public static final Type<AvatarProfiling> TYPE = new Type<>("AVATAR_PROFILING", AvatarProfiling::new);

    public final Map<ProfilingCategory, Measurer> measurers = new HashMap<>();

    public AvatarProfiling(Avatar<?> avatar, AvatarModules modules) {

    }

    // TODO: Measurers in hierarchy, killing the avatar if running average exceeds a threshold, etc...

    // Record time taken by this avatar in the given category
    public void recordTime(ProfilingCategory category, long elapsedNanos) {
        measurers.computeIfAbsent(category, x -> x == ProfilingCategory.INITIALIZATION ? new Measurer(1) : new Measurer(100)).recordTime(elapsedNanos);
    }

    // Measures a profiling category, tracking information about it
    public static class Measurer {
        private final long[] lastNValues; // Sliding window of elapsed times in nanoseconds
        private int index; // Current index into lastNValues (ring buffer)
        private long sum; // Sum of times in lastNValues

        public Measurer(int slidingWindowSize) {
            this.lastNValues = new long[slidingWindowSize];
            this.index = 0;
            this.sum = 0;
        }

        // Record a time into the sliding window of measurements
        public void recordTime(long time) {
            long prev = lastNValues[index];
            sum += (time - prev);
            lastNValues[index] = time;
            index = (index + 1) % lastNValues.length;
        }

        // Running average time over the last <slidingWindowSize> measurements
        public long runningAverage() {
            return sum / lastNValues.length;
        }
    }

}
