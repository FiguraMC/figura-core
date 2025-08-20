package org.figuramc.figura_core.util;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class MathUtils {

    // Zero and one vectors. Uses Vector3fc immutable interface,
    // so unless someone intentionally casts it, it can't be modified accidentally
    public static final Vector3fc ZERO = new Vector3f(0f);
    public static final Vector3fc ONE = new Vector3f(1f);

    public static boolean isInteger(double d) {
        return isInteger(d, 1e-5); // Reasonable default epsilon
    }
    public static boolean isInteger(double d, double epsilon) {
        return Math.abs(d - Math.round(d)) <= epsilon;
    }

    public static int smallestEncompassingPowerOfTwo(int n) {
        if (n > (1 << 30)) throw new IllegalArgumentException();
        int r = 1;
        while (n > r) r <<= 1;
        return r;
    }

    public static final float DEG_TO_RAD = (float) (Math.PI / 180);
    public static final float RAD_TO_DEG = (float) (180 / Math.PI);



}
