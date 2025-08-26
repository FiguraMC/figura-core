package org.figuramc.figura_core.model.part.tasks;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.model.part.PartTransform;
import org.figuramc.figura_core.model.part.Transformable;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

/**
 * Render tasks are not batched like model part vertices are.
 * Instead, they have to be specially drawn each frame by Minecraft.
 * Therefore, they might be a bit slower for rendering than using
 * model parts, depending on the situation; however the difference
 * will most likely be negligible for most cases.
 */
public abstract sealed class RenderTask<Self extends RenderTask<Self>> implements Transformable permits TextTask {

    public final PartTransform transform;

    public RenderTask(@Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        this.transform = new PartTransform(allocationTracker);
    }

    // Copy constructor
    public RenderTask(RenderTask<Self> task, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        this.transform = new PartTransform(task.transform, allocationTracker);
    }

    public abstract Self copy(@Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError;

    @Override
    public PartTransform getTransform() {
        return transform;
    }
}
