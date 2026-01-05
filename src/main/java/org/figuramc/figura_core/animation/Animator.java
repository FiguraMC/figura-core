package org.figuramc.figura_core.animation;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.model.part.PartTransform;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * An Animator is controlled by an Animation Instance. Sequence of events:
 * - AnimationInstance.setTime(whatever)
 * - This will mark all its Animators as dirty (in the appropriate channels).
 * - When an Animator is marked as dirty, it also marks all its PartTransforms as dirty (in the same channels).
 * - When a PartTransform is checked, it will check if it's dirty, and if so, recalculate.
 * - When recalculating, it will check its Animators.
 * - When an Animator is checked, it will check if it's dirty, and if so, recalculate.
 * - It does this by checking the AnimationInstance for the current time, and interpolating through its keyframes.
 * - The Animator is refreshed and not dirty anymore
 * - The PartTransform is refreshed and not dirty anymore
 * - Results are cached until the next setTime(), or other dirtying event
 */
public class Animator {

    // Where to source time
    private final AnimationInstance instance;
    // Source of keyframes
    private final Animation.TransformKeyframes keyframes;

    private final Vector3f origin, rotation, scale;

    // Flags, similar to PartTransform, except only using the origin/rotation/scale dirty flags.
    private byte flags = 0;
    private final byte usedChannels; // Flags for the channels this uses

    // The transforms that this Animator is placed on.
    // When the Animator updates in a channel, it should mark these transforms as dirty in that channel.
    private final List<PartTransform> transforms = new ArrayList<>(1); // Most Animators will have exactly 1 transform

    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 6
            + AllocationTracker.VEC3F_SIZE * 3
            + AllocationTracker.BYTE_SIZE * 2;

    private final @Nullable AllocationTracker.State<AvatarOutOfMemoryError> allocState;

    public Animator(AnimationInstance instance, Animation.TransformKeyframes keyframes, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        this.instance = instance;
        this.keyframes = keyframes;
        // Detect which keyframe channels are in use, set up accordingly
        byte usedChannels = PartTransform.MATRIX_DIRTY; // Matrix is always dirty, no matter which channels are in use
        if (keyframes.origin() != null && !keyframes.origin().isEmpty()) { this.origin = new Vector3f(); usedChannels |= PartTransform.ORIGIN_DIRTY; } else this.origin = null;
        if (keyframes.rotation() != null && !keyframes.rotation().isEmpty()) { this.rotation = new Vector3f(); usedChannels |= PartTransform.ROTATION_DIRTY; } else this.rotation = null;
        if (keyframes.scale() != null && !keyframes.scale().isEmpty()) { this.scale = new Vector3f(); usedChannels |= PartTransform.SCALE_DIRTY; } else this.scale = null;
        this.usedChannels = usedChannels;
        // Track instance, save state for updates
        if (allocationTracker != null) allocState = allocationTracker.track(this, SIZE_ESTIMATE); else allocState = null;
    }

    public boolean hasOrigin() { return origin != null; }
    public boolean hasRotation() { return rotation != null; }
    public boolean hasScale() { return scale != null; }

    // Getters will return null if the corresponding has() is false, so check that first!
    public Vector3fc getOrigin() {
        if (!hasOrigin()) return MathUtils.ZERO;
        if (hasFlags(PartTransform.ORIGIN_DIRTY)) {
            assert keyframes.origin() != null;
            Vec3Keyframe.evaluateTimelineInto(origin, keyframes.origin(), instance.getTime(), instance);
            origin.mul(instance.getStrength()); // Apply strength
            removeFlags(PartTransform.ORIGIN_DIRTY);
        }
        return origin;
    }

    public Vector3fc getEulerRad() {
        if (!hasRotation()) return MathUtils.ZERO;
        if (hasFlags(PartTransform.ROTATION_DIRTY)) {
            assert keyframes.rotation() != null;
            Vec3Keyframe.evaluateTimelineInto(rotation, keyframes.rotation(), instance.getTime(), instance);
            rotation.mul(instance.getStrength() * (float) (Math.PI / 180)); // Apply strength, and convert to radians
            removeFlags(PartTransform.ROTATION_DIRTY);
        }
        return rotation;
    }

    public Vector3fc getScale() {
        if (!hasScale()) return MathUtils.ONE;
        if (hasFlags(PartTransform.SCALE_DIRTY)) {
            assert keyframes.scale() != null;
            Vec3Keyframe.evaluateTimelineInto(scale, keyframes.scale(), instance.getTime(), instance);
            // Apply strength. It's a bit weird for scale, it needs to be centered around 1.
            if (instance.getStrength() != 1.0f) scale.sub(MathUtils.ONE).mul(instance.getStrength()).add(MathUtils.ONE);
            removeFlags(PartTransform.SCALE_DIRTY);
        }
        return scale;
    }

    public void markDirty() {
        this.flags = usedChannels;
        for (PartTransform transform : transforms)
            transform.setFlags(usedChannels);
    }
    private boolean hasFlags(int flags) { return (this.flags & flags) == flags; }
    private void removeFlags(int flags) { this.flags &= (byte) ~flags; }

    // PartTransform and Animator REFERENCE EACH OTHER!
    // This is so that Animator can mark PartTransform as dirty, and PartTransform can query Animator for its values!
    public void addTransform(PartTransform transform) throws AvatarOutOfMemoryError {
        if (allocState != null) allocState.changeSize(AllocationTracker.REFERENCE_SIZE);
        this.transforms.add(transform);
    }

}
