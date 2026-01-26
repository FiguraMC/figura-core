package org.figuramc.figura_core.minecraft_interop.render;

import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;

/**
 * Holds state needed by the client for rendering.
 * It's constructed given a RenderData and an allocation tracker.
 */
public interface ClientPartRenderer extends AutoCloseable {
    // Close should not error
    @Override void close();

    // Draw! The RenderData's transforms are updated by the time this runs.
    // Also passes through some kind of unknown "state" object.
    // This object should be defined by the client.
    // The object is passed unmodified through FiguraModelPart.render() into all ClientPartRenderer.draw() invocations.
    void draw(FiguraTransformStack transformStack, Object state);
}
