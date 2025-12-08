package org.figuramc.figura_core.animation;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * An Animation is a mapping from (part path) to (keyframe info).
 * An Animation can be BOUND to a RiggedHierarchy to create an AnimationInstance.
 * An AnimationInstance tracks a timer, speed, etc, and updates its Animators.
 * An Animator is placed on a PartTransform, and modifies it.
 * - When an Animator is updated, mark transforms' fields as dirty!
 */
public class Animation {

    public final Map<String, TransformKeyframes> keyframesByPartPath;

    // Construct an animation from materials
    public Animation(@Nullable String modelName, String animName, ModuleMaterials.AnimationMaterials materials, Molang molangState, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        keyframesByPartPath = MapUtils.mapValues(materials.transformKeyframes(), (partName, transformMats) -> new TransformKeyframes(
                ListUtils.map(transformMats.origin(), mats -> new Vec3Keyframe(modelName, animName, partName, mats, molangState, allocationTracker)),
                ListUtils.map(transformMats.rotation(), mats -> new Vec3Keyframe(modelName, animName, partName, mats, molangState, allocationTracker)),
                ListUtils.map(transformMats.scale(), mats -> new Vec3Keyframe(modelName, animName, partName, mats, molangState, allocationTracker))
        ));

        // Track all in one big track() call...
        // Should hopefully reduce strain on reference queue?
        // TODO make this better somehow :/
        //      If the lists ever grow/shrink then we need to handle it,
        //      and if not, we shouldn't track all the Vec3Keyframe instances separately like we do now
        if (allocationTracker != null) {
            int totalSize = AllocationTracker.OBJECT_SIZE * 2 + AllocationTracker.REFERENCE_SIZE;
            for (var entry : keyframesByPartPath.entrySet()) {
                // Track size of string keys
                totalSize += AllocationTracker.OBJECT_SIZE + entry.getKey().length() * AllocationTracker.CHAR_SIZE;
                // Track size of lists...
                if (entry.getValue().origin != null) totalSize += AllocationTracker.OBJECT_SIZE + entry.getValue().origin.size() * AllocationTracker.REFERENCE_SIZE;
                if (entry.getValue().rotation != null) totalSize += AllocationTracker.OBJECT_SIZE + entry.getValue().rotation.size() * AllocationTracker.REFERENCE_SIZE;
                if (entry.getValue().scale != null) totalSize += AllocationTracker.OBJECT_SIZE + entry.getValue().scale.size() * AllocationTracker.REFERENCE_SIZE;
            }
            allocationTracker.track(this, totalSize);
        }

        // TODO length / loop mode stuff
        // TODO script keyframes
    }

    // Sorted lists. If a field is null, then that channel is unaffected
    public record TransformKeyframes(
            @Nullable ArrayList<Vec3Keyframe> origin,
            @Nullable ArrayList<Vec3Keyframe> rotation,
            @Nullable ArrayList<Vec3Keyframe> scale
    ) {}

}
