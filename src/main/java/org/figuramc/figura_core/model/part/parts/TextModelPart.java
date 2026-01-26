package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.components.RenderDataHolder;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.model.rendering.RenderData;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Not done yet, impl is commented out ahead of commit
/**
 * A FiguraModelPart that contains text to draw!
 */
public class TextModelPart extends FiguraModelPart {

//    // The current text to be drawn
//    private FormattedText text;
//    // Whether the text changed since this was last drawn.
//    // Starts true since the original piece of text hasn't been drawn yet.
//    private boolean textChanged = true;

    public TextModelPart(Avatar<?> owningAvatar, String name, FormattedText text, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        super(owningAvatar, name, List.of());
//        this.text = text;
    }

//    public void setText(FormattedText text) {
//        this.text = text;
//        this.textChanged = true;
//    }
//
//    @Override
//    public void buildRenderingData() throws AvatarError, AvatarOutOfMemoryError {
//        // Close the old render data, if there was any
//        if (this.renderData != null) this.renderData.close();
//        // Create new render data. Bulk of the code is in addText()
//        RenderData.Builder builder = RenderData.builder();
//        builder.addText(text);
//        this.renderData = builder.build(owningAvatar.getComponent(RenderDataHolder.TYPE), owningAvatar.allocationTracker);
//    }
//
//    @Override
//    public void render(FiguraTransformStack transformStack, Object state) throws AvatarError, AvatarOutOfMemoryError {
//        // Rebuild the vertices if necessary
//        if (text.isDynamic() || textChanged) {
//            textChanged = false;
//            this.buildRenderingData();
//        }
//        assert renderData != null;
//        // Draw the vertices with our transform stack
//        transformStack.push();
//        transform.affect(transformStack);
//        renderData.clientPartRenderer.draw(transformStack, state);
//        transformStack.pop();
//    }
}
