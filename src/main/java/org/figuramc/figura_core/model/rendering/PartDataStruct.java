package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

/**
 * Represents a "PartData" object which will be collected by a tree-walking pass and used for rendering,
 * either in a VBO-based renderer or a compatible renderer.
 */
public class PartDataStruct {

    private static final int MAT4_SIZE = 4 * 4 * Float.BYTES;
    private static final int MAT3_SIZE = 3 * 4 * Float.BYTES; // 3x4 for alignment in opengl
    private static final int VEC4_SIZE = 4 * Float.BYTES;
    private static final int VEC2_SIZE = 2 * Float.BYTES;

    // Size of a PartDataStruct in bytes (gpu)
    public static final int GPU_SIZE =
            // Position mat, normal mat, color, light UV, padding
            MAT4_SIZE + MAT3_SIZE + VEC4_SIZE + VEC2_SIZE + VEC2_SIZE;

    // Size of PartDataStruct instance in bytes (cpu)
    public static final int CPU_SIZE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 3
            + AllocationTracker.MAT4F_SIZE
            + AllocationTracker.MAT3F_SIZE
            + AllocationTracker.VEC4F_SIZE
            + AllocationTracker.OBJECT_SIZE + AllocationTracker.FLOAT_SIZE * 2;

    public final Matrix4f transform = new Matrix4f();
    public final Matrix3f normalMat = new Matrix3f();
    public final Vector4f colorMultiplier = new Vector4f();
    public final Vector2f lightValues = new Vector2f();

    public void fillFromStack(FiguraTransformStack stack, boolean visible) {
        if (visible) {
            this.transform.set(stack.peekPosition());
            this.normalMat.set(stack.peekNormal());
            this.colorMultiplier.set(stack.peekColor());
            this.lightValues.set(stack.peekLight());
        } else {
            // If invisible, turn the matrix into the zero-scale matrix
            this.transform.zero();
            this.transform.set(3, 3, 1.0f);
            this.normalMat.zero();
        }
    }

    public void write(ByteBuffer buffer, int byteIndex) {
        transform.get(byteIndex, buffer); byteIndex += MAT4_SIZE;
        customGet3x4(normalMat, byteIndex, buffer); byteIndex += MAT3_SIZE; // 3x4 for alignment in opengl
        colorMultiplier.get(byteIndex, buffer); byteIndex += VEC4_SIZE;
        lightValues.get(byteIndex, buffer);
    }

    // Matrix3f.get3x4() is broken in Minecraft's version of joml lmao
    // So we reimplement it ourselves custom
    private void customGet3x4(Matrix3f matrix, int index, ByteBuffer buffer) {
        buffer.putFloat(index, matrix.m00);
        buffer.putFloat(index + 4, matrix.m01);
        buffer.putFloat(index + 8, matrix.m02);
        buffer.putFloat(index + 12, 0.0f);
        buffer.putFloat(index + 16, matrix.m10);
        buffer.putFloat(index + 20, matrix.m11);
        buffer.putFloat(index + 24, matrix.m12);
        buffer.putFloat(index + 28, 0.0f);
        buffer.putFloat(index + 32, matrix.m20);
        buffer.putFloat(index + 36, matrix.m21);
        buffer.putFloat(index + 40, matrix.m22);
        buffer.putFloat(index + 44, 0.0f);
    }

}
