package org.figuramc.figura_core.model.rendering.vertex;

import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.figura_core.util.data_structures.ByteArrayBuilder;
import org.figuramc.figura_core.util.data_structures.ByteBufferBuilder;
import org.figuramc.figura_core.util.data_structures.FloatArrayBuilder;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Holder for part vertex data, indexed by FiguraVertexElem.
 * A model part will hold vertex data for various VertexElem.
 */
public class PartVertexData {

    // When a VertexElem no longer exists, we can drop any of its associated data as well, so this is a weak map.
    // The byte[]s are densely packed with no padding.
    public final float[] positions; // 3 floats = 1 pos
    public final byte[] riggingWeights; // 4 Unsigned bytes to be normalized
    public final byte[] riggingOffsets; // 4 Unsigned non-normalized bytes, 0 to 255. 255 (-1) is a sentinel value, indicating no transform.
    public final float[] uvs; // 2 floats = 1 uv coord
    public final byte[] normals; // 3 Signed normalized bytes, -127 to 127
    // Use the hashmap for other vertex elements
    public final WeakHashMap<FiguraVertexElem, byte[]> dataByElement = new WeakHashMap<>();
    public final int vertexCount;

    private <Err extends Throwable> PartVertexData(Builder builder, @Nullable AllocationTracker<Err> allocationTracker) throws Err {
        if (builder.doneThisVertex != 0) throw new IllegalStateException("Did not end last vertex");
        this.positions = builder.positions.toArray();
        this.riggingWeights = builder.riggingWeights.toArray();
        this.riggingOffsets = builder.riggingOffsets.toArray();
        this.uvs = builder.uvs.toArray();
        this.normals = builder.normals.toArray();
        if (allocationTracker != null) {
            allocationTracker.track(positions);
            allocationTracker.track(riggingWeights);
            allocationTracker.track(riggingOffsets);
            allocationTracker.track(uvs);
            allocationTracker.track(normals);
        }
        this.vertexCount = builder.vertexCount;
    }

    public <Err extends Throwable> PartVertexData(PartVertexData copyFrom, @Nullable AllocationTracker<Err> allocationTracker) throws Err {
        for (var entry : copyFrom.dataByElement.entrySet()) {
            byte[] copiedVertices = entry.getValue().clone();
            if (allocationTracker != null) allocationTracker.track(copiedVertices);
            dataByElement.put(entry.getKey(), copiedVertices);
        }
        this.positions = copyFrom.positions.clone();
        this.riggingWeights = copyFrom.riggingWeights.clone();
        this.riggingOffsets = copyFrom.riggingOffsets.clone();
        this.uvs = copyFrom.uvs.clone();
        this.normals = copyFrom.normals.clone();
        this.vertexCount = copyFrom.vertexCount;
    }

    public static Builder builder() { return new Builder(); }

    // Builder pattern.
    // Only works with default vertex elements; others must be filled in by the avatar through script.
    public static class Builder {
        private final FloatArrayBuilder positions = new FloatArrayBuilder();
        private final ByteArrayBuilder riggingWeights = new ByteArrayBuilder();
        private final ByteArrayBuilder riggingOffsets = new ByteArrayBuilder();
        private final FloatArrayBuilder uvs = new FloatArrayBuilder();
        private final ByteArrayBuilder normals = new ByteArrayBuilder();
        private byte doneThisVertex = 0;
        private int vertexCount;

        public <Err extends Throwable> PartVertexData build(@Nullable AllocationTracker<Err> allocationTracker) throws Err {
            return new PartVertexData(this, allocationTracker);
        }

        public void endVertex() {
            if (doneThisVertex != 31)
                throw new IllegalStateException("Not all vertex elements were filled");
            doneThisVertex = 0;
            vertexCount++;
        }

        public Builder position(float x, float y, float z) {
            if ((doneThisVertex & 1) != 0) throw new IllegalStateException("Attempt to set position twice in one vertex");
            doneThisVertex |= 1;
            positions.push(x);
            positions.push(y);
            positions.push(z);
            return this;
        }

        public Builder riggingWeights(float a, float b, float c, float d) {
            if ((doneThisVertex & 2) != 0) throw new IllegalStateException("Attempt to set rigging weights twice in one vertex");
            doneThisVertex |= 2;
            riggingWeights.push(MathUtils.floatToUnsignedByte(a));
            riggingWeights.push(MathUtils.floatToUnsignedByte(b));
            riggingWeights.push(MathUtils.floatToUnsignedByte(c));
            riggingWeights.push(MathUtils.floatToUnsignedByte(d));
            return this;
        }

        public Builder riggingOffsets(byte a, byte b, byte c, byte d) {
            if ((doneThisVertex & 4) != 0) throw new IllegalStateException("Attempt to set rigging weights twice in one vertex");
            doneThisVertex |= 4;
            riggingOffsets.push(a);
            riggingOffsets.push(b);
            riggingOffsets.push(c);
            riggingOffsets.push(d);
            return this;
        }

        public Builder uv(float u, float v) {
            if ((doneThisVertex & 8) != 0) throw new IllegalStateException("Attempt to set UV twice in one vertex");
            doneThisVertex |= 8;
            uvs.push(u);
            uvs.push(v);
            return this;
        }

        public Builder normal(float x, float y, float z) {
            if ((doneThisVertex & 16) != 0) throw new IllegalStateException("Attempt to set normal twice in one vertex");
            doneThisVertex |= 16;
            normals.push(MathUtils.floatToSignedByte(x));
            normals.push(MathUtils.floatToSignedByte(y));
            normals.push(MathUtils.floatToSignedByte(z));
            return this;
        }

    }


}
