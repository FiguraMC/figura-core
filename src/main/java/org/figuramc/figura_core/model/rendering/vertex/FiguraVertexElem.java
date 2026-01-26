package org.figuramc.figura_core.model.rendering.vertex;

/**
 * A possible element of a vertex format.
 */
public class FiguraVertexElem {

    // Default vertex elements. Always present in model part data.
    // Any custom vertex elements are optionally added to model part data by scripts.
    public static final FiguraVertexElem POSITION = new FiguraVertexElem(ID.POSITION, VertexElementType.FLOAT32_3); // 12 bytes
    public static final FiguraVertexElem RIGGING_WEIGHTS = new FiguraVertexElem(ID.RIGGING_WEIGHTS, VertexElementType.UFLOAT8_4); // 4 bytes
    public static final FiguraVertexElem RIGGING_INDICES = new FiguraVertexElem(ID.RIGGING_INDICES, VertexElementType.UINT16_4); // 8 bytes
    public static final FiguraVertexElem UV0 = new FiguraVertexElem(ID.UV0, VertexElementType.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV1 = new FiguraVertexElem(ID.UV1, VertexElementType.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV2 = new FiguraVertexElem(ID.UV2, VertexElementType.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem UV3 = new FiguraVertexElem(ID.UV3, VertexElementType.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem NORMAL = new FiguraVertexElem(ID.NORMAL, VertexElementType.SFLOAT8_3); // 3 bytes
    public static final FiguraVertexElem TANGENT = new FiguraVertexElem(ID.TANGENT, VertexElementType.SFLOAT8_3); // 3 bytes
    public static final FiguraVertexElem COLOR = new FiguraVertexElem(ID.COLOR, VertexElementType.UFLOAT8_4); // 4 bytes

    public final ID id;
    public final VertexElementType type;

    private FiguraVertexElem(ID id, VertexElementType type) {
        this.id = id;
        this.type = type;
    }

    public FiguraVertexElem(VertexElementType type) {
        this(ID.OTHER, type);
    }

    // All the different IDs
    public enum ID {
        POSITION,
        RIGGING_WEIGHTS,
        RIGGING_INDICES,
        UV0, // UVs must be together in order, since we use "ordinal() - UV0.ordinal()" as index
        UV1,
        UV2,
        UV3,
        NORMAL,
        TANGENT,
        COLOR,
        OTHER // Other used as a catchall for custom elements
    }

    // Default equals/hashcode

}
