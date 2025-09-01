package org.figuramc.figura_core.model.rendering.vertex;

import org.figuramc.figura_core.util.data_structures.Pair;

public class FiguraVertexFormat {

    public static final FiguraVertexFormat DEFAULT = new FiguraVertexFormat(
            Pair.of("Position", FiguraVertexElem.POSITION),
            Pair.of("RiggingWeights", FiguraVertexElem.RIGGING_WEIGHTS),
            Pair.of("RiggingIndices", FiguraVertexElem.RIGGING_INDICES),
            Pair.of("UV", FiguraVertexElem.UV),
            Pair.of("Normal", FiguraVertexElem.NORMAL)
    );
    public static final FiguraVertexFormat POSITION = new FiguraVertexFormat(
            Pair.of("Position", FiguraVertexElem.POSITION),
            Pair.of("RiggingWeights", FiguraVertexElem.RIGGING_WEIGHTS),
            Pair.of("RiggingIndices", FiguraVertexElem.RIGGING_INDICES)
    );

    public final String[] names;
    public final FiguraVertexElem[] elements;
    public final int[] offsets;
    public final int vertexSize;

    // Construct a vertex format from named elements.
    @SafeVarargs
    public FiguraVertexFormat(Pair<String, FiguraVertexElem>... elems) {
        // Setup arrays
        this.names = new String[elems.length];
        this.elements = new FiguraVertexElem[elems.length];
        this.offsets = new int[elems.length];
        // Loop
        int size = 0;
        for (int i = 0; i < elems.length; i++) {
            names[i] = elems[i].a();
            elements[i] = elems[i].b();
            while (size % elements[i].kind.align != 0) size++;
            offsets[i] = size;
            size += elements[i].kind.size;
        }
        while (size % 4 != 0) size++; // Pad entire struct to 4
        this.vertexSize = size;
    }

}
