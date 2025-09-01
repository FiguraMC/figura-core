package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.data_structures.ByteBufferBuilder;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * A class holding on to vertex data and matrix transforms for the related model part and its children.
 */
public class RenderingRoot<T extends FiguraModelPart> {

    // The associated model part
    public final T rootPart;
    // Compressed vertex data in native-order byte buffer. Only set after calling rebuildVertices()
    public ByteBuffer builtVertexData;
    // Drawcalls, updated whenever rebuildVertices() is run
    public List<DrawCall> drawCalls = List.of();
    // CPU-side array of transforms, updated whenever calculateTransforms is run
    public PartDataStruct[] transforms = new PartDataStruct[0];
    public int transformCount; // The actual used size of transforms

    // A client state object stored by the client-side code.
    // Used for caching / storing relevant data for the client's rendering process.
    // Will be closed when this rendering root is destroyed, cleaning any native resources.
    public @Nullable Destructible clientState = null;

    // Alloc tracker (saved)
    private final @Nullable AllocationTracker<AvatarError> allocationTracker;

    public RenderingRoot(T rootPart, @Nullable AllocationTracker<AvatarError> allocationTracker) {
        this.rootPart = rootPart;
        this.allocationTracker = allocationTracker;
    }

    // Represents a slice of builtVertexData with the given type, start, and length
    public record DrawCall(FiguraRenderType renderType, int start, int length) {}

    // Rebuild this rendering root after making a change that requires it
    // (Updating vertices, updating a render type, etc.)
    public void rebuildVertices() throws AvatarError {
        // Map figura render type -> buffer builder
        Map<FiguraRenderType, ByteBufferBuilder> bufferBuilders = new HashMap<>();
        // Recursively traverse the part
        buildVerticesRecursive(rootPart, bufferBuilders, null, -Integer.MAX_VALUE, new int[1]);
        // Get all the rendertypes and their vertices, sorted by rendertype priority
        List<Map.Entry<FiguraRenderType, ByteBufferBuilder>> buffers = bufferBuilders.entrySet().stream()
                .sorted(Comparator.comparing(x -> x.getKey().priority())).toList();
        // Get total size
        int totalSize = buffers.stream().map(Map.Entry::getValue).mapToInt(ByteBufferBuilder::size).sum();
        // Create/reuse byte buffer
        if (builtVertexData != null && totalSize < builtVertexData.capacity()) {
            builtVertexData.rewind();
        } else {
            ByteBuffer buf = ByteBuffer.allocateDirect(totalSize).order(ByteOrder.nativeOrder());
            if (allocationTracker != null) allocationTracker.track(buf, totalSize);
            this.builtVertexData = buf;
        }
        drawCalls = new ArrayList<>(buffers.size());
        // Iterate, push, and generate
        int pos = 0;
        for (var entry : buffers) {
            FiguraRenderType renderType = entry.getKey();
            ByteBufferBuilder builder = entry.getValue();
            int size = builder.size();
            drawCalls.add(new DrawCall(renderType, pos, size));
            pos += size;
            builder.writeTo(builtVertexData);
        }
    }

    // Recompute the PartDataStruct[] and transformCount for this
    public void calculateTransforms(FiguraTransformStack transformStack) throws AvatarError {
        int[] arr = new int[1];
        calculateTransformsRecursive(rootPart, transformStack, true, arr);
        this.transformCount = arr[0];
    }

    // Type for the state
    public interface Destructible {
        // Should eventually clear any native resources once this is called.
        // Will only be called once by this RenderingRoot.
        void destroy();
    }

    public synchronized void destroy() {
        if (this.clientState != null) {
            this.clientState.destroy();
            this.clientState = null;
        }
    }


    // Expensive :( but that's okay
    private void buildVerticesRecursive(
            FiguraModelPart part, // Part to build vertices of
            Map<FiguraRenderType, ByteBufferBuilder> bufferBuilders, // Mapping from render type -> buffer builders
            @Nullable FiguraRenderType currentRenderType, // Current render type, if any
            int currentRenderTypePriority, // Parent's render type priority
            int[] currentId // The current ID for vertex weights stuff. Int array for mutability
    ) {
        // Update render types if we can and this part has priority
        if (part.renderType != null && part.renderTypePriority >= currentRenderTypePriority) {
            currentRenderType = part.renderType;
            currentRenderTypePriority = part.renderTypePriority;
        }
        // If this part has vertices, grab an ID
        if (part.vertices != null) {
            int id = currentId[0]++;
            // If it has a rendertype, grab the buffer and add vertices to it
            if (currentRenderType != null) {
                ByteBufferBuilder buffer = bufferBuilders.computeIfAbsent(currentRenderType, __ -> new ByteBufferBuilder());
                // Copy any needed items over.
                FiguraVertexFormat vertexFormat = currentRenderType.vertexFormat();
                // For each vertex:
                for (int i = 0; i < part.vertices.vertexCount; i++) {
                    // For each element of the vertex format, push data for it from this vertex.
                    int start = buffer.size();
                    for (int j = 0; j < vertexFormat.elements.length; j++) {
                        while (buffer.size() < start + vertexFormat.offsets[j]) { buffer.push((byte) 0); } // Alignment
                        FiguraVertexElem elem = vertexFormat.elements[j];
                        switch (elem.id) {
                            case 0 -> buffer
                                    .pushFloat(part.vertices.positions[i*3])
                                    .pushFloat(part.vertices.positions[i*3+1])
                                    .pushFloat(part.vertices.positions[i*3+2]);
                            case 1 -> buffer
                                    .push(part.vertices.riggingWeights[i*4])
                                    .push(part.vertices.riggingWeights[i*4+1])
                                    .push(part.vertices.riggingWeights[i*4+2])
                                    .push(part.vertices.riggingWeights[i*4+3]);
                            case 2 -> buffer
                                    // TODO ensure this char-cast doesn't overflow from large id! Error if it does.
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+1] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+1]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+2] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+2]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+3] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+3]));
                            case 3 -> buffer
                                    .pushFloat(part.vertices.uvs[i*2])
                                    .pushFloat(part.vertices.uvs[i*2+1]);
                            case 4 -> buffer
                                    .push(part.vertices.normals[i*3])
                                    .push(part.vertices.normals[i*3+1])
                                    .push(part.vertices.normals[i*3+2]);
                            default -> {
                                byte @Nullable[] data = part.vertices.dataByElement.get(elem);
                                if (data == null) {
                                    // Zeroes by default.
                                    for (int k = 0; k < elem.kind.size; k++) buffer.push((byte) 0);
                                } else {
                                    buffer.pushArr(data, i * elem.kind.size, elem.kind.size);
                                }
                            }
                        }
                    }
                    while (buffer.size() < start + vertexFormat.vertexSize) { buffer.push((byte) 0); } // Alignment
                }
            }
        }
        // Recurse on children
        for (var child : part.children) {
            buildVerticesRecursive(child, bufferBuilders, currentRenderType, currentRenderTypePriority, currentId);
        }
    }

    // Cheap :D
    private void calculateTransformsRecursive(
            FiguraModelPart part,
            FiguraTransformStack transformStack,
            boolean currentlyVisible,
            int[] currentId
    ) throws AvatarError {
        currentlyVisible = currentlyVisible && part.transform.getVisible();
        transformStack.push();
        if (currentlyVisible) part.transform.affect(transformStack);
        // Only parts with vertices need IDs
        if (part.vertices != null) {
            int id = currentId[0]++;
            // Resize transforms if needed
            if (transforms.length == id) {
                transforms = Arrays.copyOf(transforms, Math.max(10, transforms.length * 2));
                for (int i = id; i < transforms.length; i++) transforms[i] = new PartDataStruct();
                if (allocationTracker != null) {
                    int size = AllocationTracker.OBJECT_SIZE + (AllocationTracker.REFERENCE_SIZE + PartDataStruct.CPU_SIZE) * transforms.length;
                    allocationTracker.track(transforms, size);
                }
            }
            transforms[id].fillFromStack(transformStack, currentlyVisible);
        }
        // Recurse to children
        for (FiguraModelPart child : part.children)
            calculateTransformsRecursive(child, transformStack, currentlyVisible, currentId);
        // Pop
        transformStack.pop();
    }



}