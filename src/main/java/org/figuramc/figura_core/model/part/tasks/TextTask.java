package org.figuramc.figura_core.model.part.tasks;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;


public final class TextTask extends RenderTask<TextTask> {

//    /**
//     * The render type is a little bit odd for Text Tasks, since by their nature they
//     * have to render using the Font Texture. So any fields of the render type
//     * involving a custom texture will not be considered.
//     * If the render type is null, Figura will pick an appropriate one automatically.
//     */
//    public @Nullable FiguraRenderType renderType;
    public FormattedText formattedText;

    public TextTask(FormattedText text, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        super(allocationTracker);
        this.formattedText = text;
    }

    public TextTask(TextTask task, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        super(task, allocationTracker);
        this.formattedText = task.formattedText;
//        this.renderType = task.renderType;
    }

    @Override
    public TextTask copy(@Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        return new TextTask(this, allocationTracker);
    }
}
