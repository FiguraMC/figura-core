package org.figuramc.figura_core.util.data_structures;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteArrayBuilder {

    private static final byte[] EMPTY = new byte[0];

    protected byte[] arr;
    protected int size;

    public ByteArrayBuilder() { this(10); }
    public ByteArrayBuilder(int initialSize) {
        if (initialSize == 0) arr = EMPTY; else arr = new byte[initialSize];
    }

    public ByteArrayBuilder push(byte val) {
        resizeToFit(size);
        arr[size++] = val;
        return this;
    }

    public byte[] toArray() {
        if (size == arr.length) return arr;
        return Arrays.copyOf(arr, size);
    }

    public void writeTo(ByteBuffer buffer) {
        buffer.put(arr, 0, size);
    }

    protected void resizeToFit(int neededIndex) {
        if (neededIndex >= arr.length) {
            arr = Arrays.copyOf(arr, Math.max(10, neededIndex * 2));
        }
    }

}
