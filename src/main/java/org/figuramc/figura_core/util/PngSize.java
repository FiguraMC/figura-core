package org.figuramc.figura_core.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for PNG size from bytes
 */
public record PngSize(int width, int height) {

    private static final long PNG_HEADER = 0x89_50_4E_47_0D_0A_1A_0AL;
    private static final int IHDR_TYPE = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    private static final int IHDR_SIZE = 13; // 13 bytes

    public static PngSize fromStream(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        if (dis.readLong() != PNG_HEADER) throw new IOException("Did not find PNG header");
        if (dis.readInt() != IHDR_SIZE) throw new IOException("Incorrect IHDR chunk size");
        if (dis.readInt() != IHDR_TYPE) throw new IOException("Incorrect IHDR chunk type");
        return new PngSize(dis.readInt(), dis.readInt());
    }

    public static PngSize fromByteArray(byte[] bytes) throws IOException {
        return fromStream(new ByteArrayInputStream(bytes));
    }


}
