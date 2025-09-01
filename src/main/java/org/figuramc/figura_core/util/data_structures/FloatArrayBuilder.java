package org.figuramc.figura_core.util.data_structures;

import java.util.Arrays;

public class FloatArrayBuilder {

    private static final float[] EMPTY = new float[0];

    private float[] arr;
    private int size;

    public FloatArrayBuilder() { this(10); }
    public FloatArrayBuilder(int initialSize) {
        if (initialSize == 0) arr = EMPTY; else arr = new float[initialSize];
    }

    public FloatArrayBuilder push(float val) {
        resizeToFit(size);
        arr[size++] = val;
        return this;
    }

    public float[] toArray() {
        if (size == arr.length) return arr;
        return Arrays.copyOf(arr, size);
    }

    protected void resizeToFit(int neededIndex) {
        if (neededIndex >= arr.length) {
            arr = Arrays.copyOf(arr, Math.max(10, neededIndex * 2));
        }
    }


}
