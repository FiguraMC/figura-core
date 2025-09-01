package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Algebraic Data Type for all Figura render types.
 * Each model part has a render type and a priority for inheritance.
 * FiguraPartRenderer instances will need to figure out how to handle these.
 */
public sealed interface FiguraRenderType {

    // The priority of this render type, relative to others in this render root.
    // Priority determines rendering order. 0 is the default priority.
    // Lower priority values will render first.
    int priority();

    // The format of vertices used by this render type
    FiguraVertexFormat vertexFormat();

    // Built-in Render types:
    record Basic(@Nullable MinecraftTexture mainTex, @Nullable MinecraftTexture emissiveTex, int priority) implements FiguraRenderType {
        @Override public FiguraVertexFormat vertexFormat() { return FiguraVertexFormat.DEFAULT; }
    }
    record EndPortal(int priority) implements FiguraRenderType {
        @Override public FiguraVertexFormat vertexFormat() { return FiguraVertexFormat.POSITION; }
    }
    record EndGateway(int priority) implements FiguraRenderType {
        @Override public FiguraVertexFormat vertexFormat() { return FiguraVertexFormat.POSITION; }
    }
}
