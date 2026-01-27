package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.components.RenderDataHolder;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
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

    // The current text to be drawn
    private FormattedText text;
    // Whether the text changed since this was last rebuilt
    private boolean textChanged = true;
    // Shared scissor state
    private FiguraRenderType.ScissorState sharedScissorState = new FiguraRenderType.ScissorState();

    public TextModelPart(Avatar<?> owningAvatar, String name, FormattedText text) throws AvatarOutOfMemoryError {
        super(owningAvatar, name, List.of());
        this.text = text;
    }

    public void setText(FormattedText text) {
        this.text = text;
        this.textChanged = true;
    }

    // Text model parts are always a rendering root.
    @Override
    protected boolean isRenderingRoot() {
        return true;
    }

    @Override
    public void buildRenderingData() throws AvatarOutOfMemoryError {
        // Close the old render data, if there was any
        if (this.renderData != null) this.renderData.close();
        // Create new render data. Bulk of the code is in addText()
        RenderData.Builder builder = RenderData.builder();
        builder.addText(text);
        var pair = builder.buildText(owningAvatar.getComponent(RenderDataHolder.TYPE), owningAvatar.allocationTracker);
        this.renderData = pair.a();
        this.sharedScissorState = pair.b().set(this.sharedScissorState);
        // Text is now up to date
        textChanged = false;
    }

    @Override
    public void render(FiguraTransformStack transformStack, Object state) throws AvatarError, AvatarOutOfMemoryError {
        // Rebuild the vertices if necessary
        if (text.isDynamic() || textChanged)
            this.buildRenderingData();
        assert renderData != null;
        // Draw the vertices with our transform stack
        transformStack.push();
        transform.affect(transformStack);
        renderData.partData[0].fillFromStack(transformStack, true);
        renderData.clientPartRenderer.draw(transformStack, state);
        transformStack.pop();
    }
}
