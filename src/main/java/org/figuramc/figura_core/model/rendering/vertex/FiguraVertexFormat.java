package org.figuramc.figura_core.model.rendering.vertex;

import org.figuramc.figura_core.util.data_structures.Pair;

import java.util.*;

public class FiguraVertexFormat {

    public static final FiguraVertexFormat DEFAULT = create(true, true, true, List.of());
    public static final FiguraVertexFormat POSITION = create(false, false, false, List.of());

    public final String[] names;
    public final FiguraVertexElem[] elements;
    public final int[] offsets;
    public final int vertexSize;

    // Safer creation. Always includes position, can optionally include uv/normal/custom elements.
    public static FiguraVertexFormat create(boolean uv, boolean normal, boolean tangent, List<Pair<String, FiguraVertexElem>> customElements) {
        List<Pair<String, FiguraVertexElem>> elements = new ArrayList<>();
        elements.add(Pair.of("Position", FiguraVertexElem.POSITION));
        elements.add(Pair.of("RiggingWeights", FiguraVertexElem.RIGGING_WEIGHTS));
        elements.add(Pair.of("RiggingIndices", FiguraVertexElem.RIGGING_INDICES));
        if (uv) elements.add(Pair.of("UV", FiguraVertexElem.UV));
        if (normal) elements.add(Pair.of("Normal", FiguraVertexElem.NORMAL));
        if (tangent) elements.add(Pair.of("Tangent", FiguraVertexElem.TANGENT));
        elements.addAll(customElements);
        return new FiguraVertexFormat(elements);
    }

    // Construct a vertex format from an existing format plus additional elements.
    // Throws an error if the same element or the same name appears twice.
    public static FiguraVertexFormat extend(FiguraVertexFormat base, List<Pair<String, FiguraVertexElem>> newElems) throws IllegalArgumentException {
        // Basic case
        if (newElems.isEmpty()) return base;
        // Check args
        Set<String> existingNames = new HashSet<>(Arrays.asList(base.names));
        Set<FiguraVertexElem> existingElements = new HashSet<>(Arrays.asList(base.elements));
        for (var pair : newElems) {
            if (existingNames.contains(pair.a())) throw new IllegalArgumentException("Duplicate vertex element name: " + pair.a());
            if (existingElements.contains(pair.b())) throw new IllegalArgumentException("Duplicate vertex element under name " + pair.a());
        }
        // Construct
        List<Pair<String, FiguraVertexElem>> list = new ArrayList<>();
        for (int i = 0; i < base.elements.length; i++)
            list.add(new Pair<>(base.names[i], base.elements[i]));
        list.addAll(newElems);
        return new FiguraVertexFormat(list);
    }

    // Unsafely construct a vertex format from named elements.
    private FiguraVertexFormat(List<Pair<String, FiguraVertexElem>> elems) {
        // Setup arrays
        this.names = new String[elems.size()];
        this.elements = new FiguraVertexElem[elems.size()];
        this.offsets = new int[elems.size()];
        // Loop
        int size = 0;
        for (int i = 0; i < elems.size(); i++) {
            names[i] = elems.get(i).a();
            elements[i] = elems.get(i).b();
            while (size % elements[i].kind.align != 0) size++;
            offsets[i] = size;
            size += elements[i].kind.size;
        }
        while (size % 4 != 0) size++; // Pad entire struct to 4
        this.vertexSize = size;
    }

    // Equality and hashcodes
    @Override
    public boolean equals(Object obj) {
        return this == obj ||
                obj instanceof FiguraVertexFormat other
                && Arrays.equals(names, other.names)
                && Arrays.equals(elements, other.elements)
                && Arrays.equals(offsets, other.offsets)
                && vertexSize == other.vertexSize;
    }

    private Integer hashcode = null;

    @Override
    public int hashCode() {
        if (hashcode != null) return hashcode;
        // a.equals(b) implies a.hashCode() == b.hashCode()
        return hashcode = Arrays.hashCode(elements);
    }
}
