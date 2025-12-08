package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.render.PartRenderer;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.part.tasks.RenderTask;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.ByteBufferBuilder;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class holding on to vertex data and matrix transforms for the related model part and its children.
 */
public class RenderingRoot<T extends FiguraModelPart> {

    // The associated model part
    public final T rootPart;
    // Compressed vertex data, by format, in native-order byte buffer. Only set after calling rebuildVertices()
    public Map<FiguraVertexFormat, ByteBuffer> builtVertexData = new HashMap<>();
    // Drawcalls, updated whenever rebuildVertices() is run
    public List<DrawCall> drawCalls = List.of();
    // CPU-side array of transforms, updated whenever calculateTransforms is run
    public PartDataStruct[] transforms = new PartDataStruct[0];
    public int transformCount; // The actual used size of transforms

    // A client state object stored by the client-side code.
    // Used for caching / storing relevant data for the client's rendering process.
    // Will be closed when this rendering root is destroyed, cleaning any native resources.
    private @Nullable PartRenderer renderer;

    // Alloc tracker (saved)
    private final @Nullable AllocationTracker<AvatarError> allocationTracker;

    public RenderingRoot(T rootPart, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        this.rootPart = rootPart;
        this.allocationTracker = allocationTracker;
        this.renderer = Objects.requireNonNull(FiguraConnectionPoint.PART_RENDERER_FACTORY.apply(this), "Null renderer was provided for RenderingRoot");
    }

    public @NotNull PartRenderer getRenderer() {
        if (renderer == null) throw new IllegalStateException("Attempt to access renderer after RenderingRoot was destroyed");
        return renderer;
    }

    // Represents a slice of builtVertexData with the given type, start, and length
    public record DrawCall(FiguraRenderType renderType, int start, int length) {}

    // Rebuild this rendering root after making a change that requires it
    // Generally this should run when rendering (because we want to run it on the render thread).
    // Figura-core shouldn't call this at all, unless we somehow know we're on the render thread.
    // Instead we should invalidate the renderer so it knows to recompute vertices.
    // Also may update the PartDataStruct[] and transformCount
    public void rebuildVertices() throws AvatarError {
        // Map figura render type -> buffer builder
        Map<FiguraRenderType, ByteBufferBuilder> bufferBuilders = new HashMap<>();
        // Recursively traverse the part
        int[] nextId = new int[1];
        buildVerticesRecursive(rootPart, bufferBuilders, null, -Integer.MAX_VALUE, nextId);
        transformCount = nextId[0];
        // Get all the rendertypes and their vertices, sorted by rendertype priority
        List<Map.Entry<FiguraRenderType, ByteBufferBuilder>> buffers = bufferBuilders.entrySet().stream()
                .sorted(Comparator.comparing(x -> x.getKey().priority())).toList();
        // Set up a drawcalls list, one item for each entry
        drawCalls = new ArrayList<>(buffers.size());
        // Get vertex buffers grouped by vertex format, and required buffer sizes
        Map<FiguraVertexFormat, List<ByteBufferBuilder>> byFormat = bufferBuilders.entrySet().stream()
                .collect(Collectors.groupingBy(e -> e.getKey().shader().vertexFormat(),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        Map<FiguraVertexFormat, Integer> requiredBufferSizes = MapUtils.mapValues(byFormat, list -> list.stream().mapToInt(ByteBufferBuilder::size).sum());
        // Remove all prior entries which aren't used anymore or are too small
        builtVertexData.entrySet().removeIf(entry ->
                !byFormat.containsKey(entry.getKey()) // Not in the new set of formats
                || entry.getValue().capacity() < requiredBufferSizes.get(entry.getKey()) // Buffer is too small
        );
        // Rewind any remaining buffers
        builtVertexData.values().forEach(ByteBuffer::rewind);
        // Iterate buffers...
        for (var entry : buffers) {
            FiguraRenderType renderType = entry.getKey();
            ByteBufferBuilder builder = entry.getValue();
            FiguraVertexFormat format = renderType.shader().vertexFormat();
            // Get or create buffer
            @Nullable ByteBuffer buffer = builtVertexData.get(format);
            if (buffer == null) {
                int size = requiredBufferSizes.get(format);
                buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
                if (allocationTracker != null) allocationTracker.track(buffer, size);
                builtVertexData.put(format, buffer);
            }
            // Put builder data into buffer
            int position = buffer.position();
            builder.writeTo(buffer);
            // Add a new draw call data.
            drawCalls.add(new DrawCall(renderType, position, builder.size()));
        }
    }

    // Traverse the model part tree and extract transforms as we go.
    // The transforms will be stored in the transforms field.
    // Passed handler(s) will be invoked during this, it's up to the client to put logic there.
    public <E1 extends Throwable, E2 extends Throwable> void extractTransforms(FiguraTransformStack transformStack, @Nullable RenderTaskHandler<E1, E2> renderTaskHandler) throws E1, E2 {
        extractTransformsRecursive(rootPart, transformStack, true, new int[1], renderTaskHandler);
    }

    public synchronized void destroy() {
        if (this.renderer != null) {
            this.renderer.destroy();
            this.renderer = null;
        }
    }

    // Expensive :( but that's okay
    private void buildVerticesRecursive(
            FiguraModelPart part, // Part to build vertices of
            Map<FiguraRenderType, ByteBufferBuilder> bufferBuilders, // Mapping from render type -> buffer builders
            @Nullable FiguraRenderType currentRenderType, // Current render type, if any
            int currentRenderTypePriority, // Parent's render type priority
            int[] currentId // The current ID for vertex weights stuff. Int array for mutability
    ) throws AvatarError {
        // Update render types if we can and this part has priority
        if (part.renderType != null && part.renderTypePriority >= currentRenderTypePriority) {
            currentRenderType = part.renderType;
            currentRenderTypePriority = part.renderTypePriority;
        }
        // If this part has vertices, grab an ID
        if (part.vertices != null) {
            int id = currentId[0]++;
            // Grow transforms array if needed
            if (transforms.length == id) {
                transforms = Arrays.copyOf(transforms, Math.max(10, transforms.length * 2));
                for (int i = id; i < transforms.length; i++) transforms[i] = new PartDataStruct();
                if (allocationTracker != null) {
                    int size = AllocationTracker.OBJECT_SIZE + (AllocationTracker.REFERENCE_SIZE + PartDataStruct.CPU_SIZE) * transforms.length;
                    allocationTracker.track(transforms, size);
                }
            }
            // If it has a rendertype, grab the buffer and add vertices to it
            if (currentRenderType != null) {
                ByteBufferBuilder buffer = bufferBuilders.computeIfAbsent(currentRenderType, __ -> new ByteBufferBuilder());
                // Copy any needed items over.
                FiguraVertexFormat vertexFormat = currentRenderType.shader().vertexFormat();
                // For each vertex:
                for (int i = 0; i < part.vertices.vertexCount; i++) {
                    // For each element of the vertex format, push data for it from this vertex.
                    int start = buffer.size();
                    for (int j = 0; j < vertexFormat.elements.length; j++) {
                        while (buffer.size() < start + vertexFormat.offsets[j]) { buffer.push((byte) 0); } // Alignment
                        FiguraVertexElem elem = vertexFormat.elements[j];
                        switch (elem.builtinId) {
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
                            case 5 -> buffer
                                    .push(part.vertices.tangents[i*3])
                                    .push(part.vertices.tangents[i*3+1])
                                    .push(part.vertices.tangents[i*3+2]);
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
    private <E1 extends Throwable, E2 extends Throwable> void extractTransformsRecursive(
            FiguraModelPart part,
            FiguraTransformStack transformStack,
            boolean currentlyVisible,
            int[] currentId,
            @Nullable RenderTaskHandler<E1, E2> renderTaskHandler
    ) throws E1, E2 {
        currentlyVisible = currentlyVisible && part.transform.getVisible();
        transformStack.push();
        if (currentlyVisible) part.transform.affect(transformStack);
        // Only parts with vertices need IDs
        if (part.vertices != null) {
            int id = currentId[0]++;
            // Resize transforms if needed
            if (id < transformCount) // If this if-statement fails, then we didn't invalidate() when we should have
                transforms[id].fillFromStack(transformStack, currentlyVisible);
        }
        // Recurse to children
        for (FiguraModelPart child : part.children)
            extractTransformsRecursive(child, transformStack, currentlyVisible, currentId, renderTaskHandler);
        // Invoke the render task handler on each render task
        if (renderTaskHandler != null) {
            for (RenderTask<?> renderTask : part.renderTasks) {
                transformStack.push();
                renderTask.transform.affect(transformStack);
                renderTaskHandler.handle(renderTask, transformStack);
                transformStack.pop();
            }
        }
        // Pop
        transformStack.pop();
    }

    // Handle a RenderTask. Callback passed to extractTransforms()
    @FunctionalInterface
    public interface RenderTaskHandler<E1 extends Throwable, E2 extends Throwable> {
        void handle(RenderTask<?> task, FiguraTransformStack stack) throws E1, E2;
    }


}