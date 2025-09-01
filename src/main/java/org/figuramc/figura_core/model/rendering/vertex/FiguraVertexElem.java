package org.figuramc.figura_core.model.rendering.vertex;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A possible element of a vertex format.
 */
public class FiguraVertexElem {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(5);

    // Default vertex elements. Always present in model part data.
    public static final FiguraVertexElem POSITION = new FiguraVertexElem(0, VertexElementKind.FLOAT32_3); // 12 bytes
    public static final FiguraVertexElem RIGGING_WEIGHTS = new FiguraVertexElem(1, VertexElementKind.UFLOAT8_4); // 4 bytes
    public static final FiguraVertexElem RIGGING_INDICES = new FiguraVertexElem(2, VertexElementKind.UINT16_4); // 8 bytes
    public static final FiguraVertexElem UV = new FiguraVertexElem(3, VertexElementKind.FLOAT32_2); // 8 bytes
    public static final FiguraVertexElem NORMAL = new FiguraVertexElem(4, VertexElementKind.SFLOAT8_3); // 3 bytes

    public final VertexElementKind kind;
    public final int id; // Switch statement go nyoom

    private FiguraVertexElem(int id, VertexElementKind kind) {
        this.id = id;
        this.kind = kind;
    }

    public FiguraVertexElem(VertexElementKind kind) {
        this(NEXT_ID.getAndIncrement(), kind);
    }

}
