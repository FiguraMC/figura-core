package org.figuramc.figura_core.minecraft_interop.model_part_renderers;

import org.figuramc.figura_core.model.part.FiguraModelPart;

/**
 * A rendering backend, which can create a FiguraModelPartRenderer, given a particular FiguraModelPart.
 * The client will supply an implementation of this.
 */
public interface FiguraModelPartRendererFactory {

    /**
     * Create a renderer that can render the given part.
     * This may be an expensive operation!
     */
    FiguraModelPartRenderer createRenderer(FiguraModelPart part);

}
