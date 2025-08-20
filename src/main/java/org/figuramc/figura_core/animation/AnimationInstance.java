package org.figuramc.figura_core.animation;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.model.part.RiggedHierarchy;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks current time, speed, etc. of an animation occurring.
 * Created by binding an Animation to a RiggedHierarchy.
 */
public class AnimationInstance {

    // Time in seconds since the start of the animation
    private float time = 0.0f;
    // Strength multiplier for effectiveness of the animation
    private float strength = 1.0f;

    // List of animators, to mark as dirty when this changes
    private final Animator[] animators;

    private static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE
            + AllocationTracker.FLOAT_SIZE * 2;

    // Bind the Animation to a RiggedHierarchy, within an Animations component
    public AnimationInstance(Animation animation, RiggedHierarchy<?> root, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        animators = new Animator[animation.keyframesByPartPath.size()];
        int i = 0;
        for (var entry : animation.keyframesByPartPath.entrySet()) {
            String partPath = entry.getKey();
            RiggedHierarchy<?> descendant = root.getDescendantWithPath(partPath);
            if (descendant == null) continue; // TODO some kind of warning when the instance doesn't fully bind?
            Animation.TransformKeyframes keyframes = entry.getValue();
            Animator animator = new Animator(this, keyframes, allocationTracker);
            descendant.getTransform().addAnimator(animator);
            animators[i++] = animator;
        }
        // Track this instance
        if (allocationTracker != null) {
            allocationTracker.track(this, SIZE_ESTIMATE + animators.length * AllocationTracker.REFERENCE_SIZE);
        }
    }

    // Mark animators as dirty, so they update when called upon
    private void markDirty() {
        for (Animator animator : animators)
            animator.markDirty();
    }

    // Get/set time
    public float getTime() {
        return time;
    }
    public void setTime(float time) {
        if (this.time != time) {
            this.time = time;
            markDirty();
        }
    }

    // Get/set strength
    public float getStrength() {
        return strength;
    }
    public void setStrength(float strength) {
        if (this.strength != strength) {
            this.strength = strength;
            markDirty();
        }
    }

}
