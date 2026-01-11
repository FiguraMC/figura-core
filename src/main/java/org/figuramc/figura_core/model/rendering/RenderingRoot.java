package org.figuramc.figura_core.model.rendering;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.render.PartRenderer;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.part.tasks.RenderTask;
import org.figuramc.figura_core.model.rendering.shader.FiguraShader;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.ByteBufferBuilder;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collector;
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
    public List<DrawCall> drawCalls = new ArrayList<>();
    // CPU-side array of transforms, updated whenever calculateTransforms is run
    public PartDataStruct[] transforms = new PartDataStruct[0];
    public int transformCount; // The actual used size of transforms

    // A client state object stored by the client-side code.
    // Used for caching / storing relevant data for the client's rendering process.
    // Will be closed when this rendering root is destroyed, cleaning any native resources.
    private @Nullable PartRenderer renderer;

    // Alloc tracker (saved)
    private final @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker;

    public RenderingRoot(T rootPart, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        this.rootPart = rootPart;
        this.allocationTracker = allocationTracker;
        this.renderer = Objects.requireNonNull(FiguraConnectionPoint.PART_RENDERER_FACTORY.apply(this), "Null renderer was provided for RenderingRoot");
    }

    public @NotNull PartRenderer getRenderer() {
        if (renderer == null) throw new IllegalStateException("Attempt to access renderer after RenderingRoot was destroyed");
        return renderer;
    }

    // Holds a slice of builtVertexData with the given draw call info, starting point, and length
    public record DrawCall(FiguraRenderType.DrawCallInfo drawCallInfo, int start, int length) {}

    // Rebuild this rendering root after making a change that requires it
    // Generally this should run when rendering (because we want to run it on the render thread).
    // Figura-core shouldn't call this at all, unless we somehow know we're on the render thread.
    // Instead we should invalidate the renderer so it knows to recompute vertices.
    // Also may update the PartDataStruct[] and transformCount
    public void rebuildVertices() throws AvatarOutOfMemoryError, AvatarError {
        // Map draw call info -> buffer builder
        Map<FiguraRenderType.DrawCallInfo, ByteBufferBuilder> bufferBuilders = new HashMap<>();

        // Recursively traverse the part
        int[] nextId = new int[1];
        buildVerticesRecursive(rootPart, bufferBuilders, null, -Integer.MAX_VALUE, nextId);
        transformCount = nextId[0];

        // Group draw call infos by priority
        var byPriority = bufferBuilders.entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().priority(), TreeMap::new, Collectors.toList()));
        // Within the same priority, group by shader
        var byShaderByPriority = MapUtils.mapValues(byPriority, samePriority -> samePriority.stream().collect(Collectors.groupingBy(e -> e.getKey().shader(), HashMap::new, Collectors.toList())));

        // Coalesce to a sorted list of pairs (DrawCallInfo, ByteBufferBuilder)
        var drawCallInfosAndBufferBuilders = byShaderByPriority.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(List::stream)
                .map(Pair::new)
                .toList();

        // Map vertex format -> list of buffer builders for that format
        var byFormat = drawCallInfosAndBufferBuilders.stream().collect(
                Collectors.groupingBy(p -> p.a().shader().vertexFormat(),
                        Collectors.mapping(Pair::b, Collectors.toList())));
        // Find the required buffer size for each vertex format
        var requiredBufferSizes = MapUtils.mapValues(byFormat, builders -> builders.stream().mapToInt(ByteBufferBuilder::size).sum());

        // Remove all ByteBuffer which aren't used anymore or are too small
        builtVertexData.entrySet().removeIf(entry ->
                !byFormat.containsKey(entry.getKey()) // Not in the new set of formats
                || entry.getValue().capacity() < requiredBufferSizes.get(entry.getKey()) // Buffer is too small
        );

        // Rewind any buffers that we're reusing
        builtVertexData.values().forEach(ByteBuffer::rewind);

        // Clear the old set of draw calls, and construct new ones
        drawCalls.clear();
        for (var pair : drawCallInfosAndBufferBuilders) {
            FiguraRenderType.DrawCallInfo drawCallInfo = pair.a();
            ByteBufferBuilder bufferBuilder = pair.b();
            // Get or create buffer
            FiguraVertexFormat format = drawCallInfo.shader().vertexFormat();
            @Nullable ByteBuffer buffer = builtVertexData.get(format);
            if (buffer == null) {
                int size = requiredBufferSizes.get(format);
                buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
                if (allocationTracker != null) allocationTracker.track(buffer, size);
                builtVertexData.put(format, buffer);
            }
            // Insert the builder's data into the buffer
            int position = buffer.position();
            bufferBuilder.writeTo(buffer);
            // Add a new draw call data to the list!
            drawCalls.add(new DrawCall(drawCallInfo, position, bufferBuilder.size()));
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

    // Kinda expensive, but we have to do it
    private void buildVerticesRecursive(
            FiguraModelPart part, // Part to build vertices of
            Map<FiguraRenderType.DrawCallInfo, ByteBufferBuilder> bufferBuilders, // Mapping from render type -> buffer builders
            @Nullable FiguraRenderType currentRenderType, // Current render type, if any
            int currentRenderTypePriority, // Parent's render type priority
            int[] currentId // The current ID for vertex weights stuff. Int array for mutability
    ) throws AvatarOutOfMemoryError, AvatarError {
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
                ByteBufferBuilder buffer = bufferBuilders.computeIfAbsent(currentRenderType.drawCallInfo(), __ -> new ByteBufferBuilder());
                // Copy any needed items over.
                FiguraVertexFormat vertexFormat = currentRenderType.shader.vertexFormat();
                // For each vertex:
                for (int i = 0; i < part.vertices.vertexCount; i++) {
                    // For each element of the vertex format, push data for it from this vertex.
                    int start = buffer.size();
                    for (int j = 0; j < vertexFormat.elements.length; j++) {
                        while (buffer.size() < start + vertexFormat.offsets[j]) { buffer.push((byte) 0); } // Alignment
                        FiguraVertexElem elem = vertexFormat.elements[j];
                        switch (elem.builtinId) {
                            case FiguraVertexElem.POSITION_ID -> buffer
                                    .pushFloat(part.vertices.positions[i*3])
                                    .pushFloat(part.vertices.positions[i*3+1])
                                    .pushFloat(part.vertices.positions[i*3+2]);
                            case FiguraVertexElem.RIGGING_WEIGHTS_ID -> buffer
                                    .push(part.vertices.riggingWeights[i*4])
                                    .push(part.vertices.riggingWeights[i*4+1])
                                    .push(part.vertices.riggingWeights[i*4+2])
                                    .push(part.vertices.riggingWeights[i*4+3]);
                            case FiguraVertexElem.RIGGING_INDICES_ID -> {
                                if (id > 0xFF00) throw new AvatarError(TOO_MANY_GROUPS, TranslatableItems.Items0.INSTANCE);
                                buffer
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+1] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+1]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+2] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+2]))
                                    .pushUnsignedShort(part.vertices.riggingOffsets[i*4+3] == -1 ? Character.MAX_VALUE : (char) (id + part.vertices.riggingOffsets[i*4+3]));
                            }
                            case FiguraVertexElem.UV0_ID, FiguraVertexElem.UV1_ID, FiguraVertexElem.UV2_ID, FiguraVertexElem.UV3_ID -> {
                                float u = part.vertices.uvs[i*2];
                                float v = part.vertices.uvs[i*2+1];
                                int uvIndex = elem.builtinId - FiguraVertexElem.UV0_ID; // Offset from UV0
                                Vector4f uvModifier = currentRenderType.textureBindings.get(uvIndex).uvModifier();
                                u = u * uvModifier.z + uvModifier.x;
                                v = v * uvModifier.w + uvModifier.y;
                                buffer.pushFloat(u).pushFloat(v);
                            }
                            case FiguraVertexElem.NORMAL_ID -> buffer
                                    .push(part.vertices.normals[i*3])
                                    .push(part.vertices.normals[i*3+1])
                                    .push(part.vertices.normals[i*3+2]);
                            case FiguraVertexElem.TANGENT_ID -> buffer
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

    private static final Translatable<TranslatableItems.Items0> TOO_MANY_GROUPS = Translatable.create("figura_core.error.rendering.too_many_groups");
}