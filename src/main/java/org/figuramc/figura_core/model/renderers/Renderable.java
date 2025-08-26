package org.figuramc.figura_core.model.renderers;

import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.model_part_renderers.FiguraModelPartRenderer;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;

/**
 * A wrapper around a model part and a renderer for it
 */
public class Renderable<T extends FiguraModelPart> {

    public final T part;
    public final FiguraModelPartRenderer renderer;

    public Renderable(T part) {
        this.part = part;
        this.renderer = FiguraConnectionPoint.MODEL_PART_RENDERER_FACTORY.createRenderer(part);
    }

    public void destroy() {
        this.renderer.destroy();
    }

}
