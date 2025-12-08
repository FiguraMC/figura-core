package org.figuramc.figura_core.minecraft_interop.render;

import org.figuramc.figura_core.model.rendering.RenderingRoot;

/**
 * A piece of state that's attached to a RenderingRoot, created by the client.
 * Contains hooks for RenderingRoot functionality.
 */
public abstract class PartRenderer {

    // The root this is connected to
    protected final RenderingRoot<?> root;

    public PartRenderer(RenderingRoot<?> root) {
        this.root = root;
    }

    // When this is run, the vertex data has been invalidated. So we should run a rebuild the next time we render!
    public abstract void invalidate();
    public abstract void destroy();
}
