package org.figuramc.figura_core.util.data_structures;

import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;

/**
 * A matrix stack that doesn't allocate when popping then pushing.
 * When calling translate(), rotate(), or scale(), it will act as though
 * it has performed that transformation *before* the transformation it
 * currently has in its stack. It will *post-multiply*. This is the same
 * behavior that JOML uses, as well as the vanilla matrix stack, so I'm
 * implementing this in the same way for convention.
 * <p>
 * This uses float precision rather than double - the reason is to potentially
 * make it easier to change to storing data on the GPU later.
 * <p>
 * Problems relating to double vs float precision should be resolved elsewhere, not
 * as part of the matrix stack.
 * <p>
 * This stack also contains other information than just the matrices, such as color info.
 */
public class FiguraTransformStack {

    private final ArrayList<Matrix4f> positionMatrices = new ArrayList<>();
    private final ArrayList<Matrix3f> normalMatrices = new ArrayList<>();
    private final ArrayList<Vector4f> colorMultipliers = new ArrayList<>();
    int curIndex; //index of the top item
    int maxSize; //the number of matrices that have been on the stack at its peak

    public FiguraTransformStack() {
        curIndex = 0;
        maxSize = 1;
        positionMatrices.add(new Matrix4f());
        normalMatrices.add(new Matrix3f());
        colorMultipliers.add(new Vector4f(1,1,1,1));
    }

    public void translate(Vector3fc vec) {
        translate(vec.x(), vec.y(), vec.z());
    }
    public void translate(float x, float y, float z) {
        positionMatrices.get(curIndex).translate(x, y, z);
    }

    public void scale(Vector3fc vec) {
        scale(vec.x(), vec.y(), vec.z());
    }
    public void scale(float x, float y, float z) {
        positionMatrices.get(curIndex).scale(x, y, z);
        if (x == y && y == z) {
            if (x > 0)
                return; //If all positive, and uniform scaling, normals are not affected
            normalMatrices.get(curIndex).scale(-1);
        }
        float f = 1 / x;
        float g = 1 / y;
        float h = 1 / z;
        float i = (float) (1 / Math.cbrt(f * g * h));
        normalMatrices.get(curIndex).scale(f * i, g * i, h * i);
    }

    public void color(Vector4f multiplier) {
        colorMultipliers.get(curIndex).mul(multiplier);
    }

    public void rotate(Quaternionf quaternion) {
        positionMatrices.get(curIndex).rotate(quaternion);
        normalMatrices.get(curIndex).rotate(quaternion);
    }

    public void multiply(Matrix4f posMatrix, Matrix3f normalMatrix) {
        positionMatrices.get(curIndex).mul(posMatrix);
        normalMatrices.get(curIndex).mul(normalMatrix);
    }

    private final Matrix3f normal = new Matrix3f();
    public void multiply(Matrix4f posMatrix) {
        positionMatrices.get(curIndex).mul(posMatrix);
        posMatrix.normal(normal);
        normalMatrices.get(curIndex).mul(normal);
    }

    public void push() {
        curIndex++;
        if (curIndex == maxSize) {
            positionMatrices.add(new Matrix4f(positionMatrices.get(curIndex - 1)));
            normalMatrices.add(new Matrix3f(normalMatrices.get(curIndex - 1)));
            colorMultipliers.add(new Vector4f(colorMultipliers.get(curIndex - 1)));
            maxSize++;
        } else if (curIndex > maxSize) {
            throw new IllegalStateException("Current index should never be above max size - this is a bug in FiguraMatrixStack!");
        } else {
            positionMatrices.get(curIndex).set(positionMatrices.get(curIndex - 1));
            normalMatrices.get(curIndex).set(normalMatrices.get(curIndex - 1));
            colorMultipliers.get(curIndex).set(colorMultipliers.get(curIndex - 1));
        }
    }

    public void pop() {
        curIndex--;
    }

    public Matrix4f peekPosition() {
        return positionMatrices.get(curIndex);
    }

    public Matrix3f peekNormal() {
        return normalMatrices.get(curIndex);
    }

    public Vector4f peekColor() {
        return colorMultipliers.get(curIndex);
    }

    public boolean isEmpty() {
        return curIndex == 0;
    }

    public void loadIdentity() {
        positionMatrices.get(curIndex).identity();
        normalMatrices.get(curIndex).identity();
    }

}