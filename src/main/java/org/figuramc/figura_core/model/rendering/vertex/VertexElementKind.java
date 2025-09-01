package org.figuramc.figura_core.model.rendering.vertex;

// Types of vertex elements
public enum VertexElementKind {
    // Raw 32 bit float and vectors thereof
    FLOAT32(4, 4), FLOAT32_2(8, 8), FLOAT32_3(12, 16), FLOAT32_4(16, 16),
    // Unsigned byte -> 32 bit float in shader. [0, 255] -> [0, 1]
    UFLOAT8(1, 4), UFLOAT8_2(2, 4), UFLOAT8_3(3, 4), UFLOAT8_4(4, 4),
    // Signed byte -> 32 bit float in shader. [-127, 127] -> [-1, 1] (-128 also goes to -1)
    SFLOAT8(1, 4), SFLOAT8_2(2, 4), SFLOAT8_3(3, 4), SFLOAT8_4(4, 4),
    // Unsigned short -> unsigned short in shader. (No conversion)
    UINT16(2, 4), UINT16_2(4, 4), UINT16_3(6, 8), UINT16_4(8, 8);

    public final int size, align;

    VertexElementKind(int size, int align) {
        this.size = size;
        this.align = align;
    }

}
