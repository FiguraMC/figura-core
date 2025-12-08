package org.figuramc.figura_core.model.rendering.vertex;

/**
 * A possible element of a vertex format.
 */
public class FiguraVertexElem {

    // Default vertex elements. Always present in model part data.
    public static final FiguraVertexElem POSITION = new FiguraVertexElem(0, VertexElementKind.FLOAT32_3); // 12 bytes
    public static final FiguraVertexElem RIGGING_WEIGHTS = new FiguraVertexElem(1, VertexElementKind.UFLOAT8_4); // 4 bytes
    public static final FiguraVertexElem RIGGING_INDICES = new FiguraVertexElem(2, VertexElementKind.UINT16_4); // 8 bytes
    public static final FiguraVertexElem UV = new FiguraVertexElem(3, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem NORMAL = new FiguraVertexElem(4, VertexElementKind.SFLOAT8_3); // 3 bytes
    public static final FiguraVertexElem TANGENT = new FiguraVertexElem(5, VertexElementKind.SFLOAT8_3); // 3 bytes

    public final VertexElementKind kind;
    public final int builtinId; // Switch statement go nyoom?

    private FiguraVertexElem(int builtinId, VertexElementKind kind) {
        this.builtinId = builtinId;
        this.kind = kind;
    }

    public FiguraVertexElem(VertexElementKind kind) {
        // Non-builtins get a default value
        this(-1, kind);
    }

    // Default equals/hashcode

}
