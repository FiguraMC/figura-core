package org.figuramc.figura_core.util.data_structures;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferBuilder extends ByteArrayBuilder {

    private static final byte[] EMPTY = new byte[0];
    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    public ByteBufferBuilder() { this(10); }
    public ByteBufferBuilder(int initialSize) {
        super(initialSize);
    }

    public int size() { return size; }


    // Push values
    public ByteBufferBuilder pushArr(byte[] newArr) {
        resizeToFit(size + newArr.length - 1);
        System.arraycopy(newArr, 0, this.arr, size, newArr.length);
        size += newArr.length;
        return this;
    }
    public ByteBufferBuilder pushArr(byte[] newArr, int start, int length) {
        resizeToFit(size + length - 1);
        System.arraycopy(newArr, start, this.arr, size, length);
        size += length;
        return this;
    }
    public ByteBufferBuilder pushNormalizedSignedByte(float value) {
        push((byte) (Math.clamp(value, -1, 1) * 127.0f));
        return this;
    }
    public ByteBufferBuilder pushNormalizedUnsignedByte(float value) {
        push((byte) (int) (Math.clamp(value, 0, 1) * 255.0f));
        return this;
    }
    public ByteBufferBuilder pushUnsignedShort(char value) {
        if (LITTLE_ENDIAN) { push((byte) (value & 0xFF)); push((byte) ((value >>> 8) & 0xFF)); }
        else { push((byte) ((value >>> 8) & 0xFF)); push((byte) (value & 0xFF)); }
        return this;
    }
    public ByteBufferBuilder pushFloat(float value) {
        int asIntBytes = Float.floatToRawIntBits(value);
        if (LITTLE_ENDIAN) {
            push((byte) (asIntBytes & 0xFF));
            push((byte) ((asIntBytes >>> 8) & 0xFF));
            push((byte) ((asIntBytes >>> 16) & 0xFF));
            push((byte) ((asIntBytes >>> 24) & 0xFF));
        } else {
            push((byte) ((asIntBytes >>> 24) & 0xFF));
            push((byte) ((asIntBytes >>> 16) & 0xFF));
            push((byte) ((asIntBytes >>> 8) & 0xFF));
            push((byte) (asIntBytes & 0xFF));
        }
        return this;
    }

    // Convert to a byte buffer with native byte order.
    // If the existing buffer has enough capacity, overwrite that one instead.
    // Otherwise, create a new buffer.
    public ByteBuffer toBuffer(boolean useNative, @Nullable ByteBuffer existingBuffer) {
        ByteBuffer b;
        if (existingBuffer != null && existingBuffer.capacity() >= size) {
            b = existingBuffer.rewind();
        } else if (useNative) {
            b = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        } else {
            b = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
        }
        b.put(this.arr, 0, size);
        return b;
    }

}
