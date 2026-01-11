package org.figuramc.figura_core.model.rendering.vertex;

/**
 * A possible element of a vertex format.
 */
public class FiguraVertexElem {

    public static final int POSITION_ID = 0;
    public static final int RIGGING_WEIGHTS_ID = 1;
    public static final int RIGGING_INDICES_ID = 2;
    public static final int UV0_ID = 3;
    public static final int UV1_ID = 4;
    public static final int UV2_ID =  5;
    public static final int UV3_ID =  6;
    public static final int NORMAL_ID = 7;
    public static final int TANGENT_ID = 8;

    // Default vertex elements. Always present in model part data.
    public static final FiguraVertexElem POSITION = new FiguraVertexElem(POSITION_ID, VertexElementKind.FLOAT32_3); // 12 bytes
    public static final FiguraVertexElem RIGGING_WEIGHTS = new FiguraVertexElem(RIGGING_WEIGHTS_ID, VertexElementKind.UFLOAT8_4); // 4 bytes
    public static final FiguraVertexElem RIGGING_INDICES = new FiguraVertexElem(RIGGING_INDICES_ID, VertexElementKind.UINT16_4); // 8 bytes
    public static final FiguraVertexElem UV0 = new FiguraVertexElem(UV0_ID, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV1 = new FiguraVertexElem(UV1_ID, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV2 = new FiguraVertexElem(UV2_ID, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV3 = new FiguraVertexElem(UV3_ID, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem NORMAL = new FiguraVertexElem(NORMAL_ID, VertexElementKind.SFLOAT8_3); // 3 bytes
    public static final FiguraVertexElem TANGENT = new FiguraVertexElem(TANGENT_ID, VertexElementKind.SFLOAT8_3); // 3 bytes

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
