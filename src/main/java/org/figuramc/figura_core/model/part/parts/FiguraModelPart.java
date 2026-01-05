package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.Materials;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.part.PartTransform;
import org.figuramc.figura_core.model.part.RiggedHierarchy;
import org.figuramc.figura_core.model.part.tasks.RenderTask;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.model.rendering.vertex.PartVertexData;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;

/**
 * Corresponds to a Group in Blockbench (or other structural elements, like directories and .figmodel files).
 * <p>
 * Unlike previously, Figura's scripting no longer allows manipulation of individual cubes and meshes, only groups.
 * Here's why:
 * - This is more in-line with Blockbench, as Blockbench only allows animations to affect groups
 * - This can be more efficient rendering-wise, because most of the time individual cubes are not articulated, allowing
 *   for less unneeded computation. When they do need to be articulated, one can simply add a group for said cube.
 */
public class FiguraModelPart implements RiggedHierarchy<FiguraModelPart> {

    // Structure / modifications
    public final String name;
    public final PartTransform transform; // The transform of this model part

//    private  animators; // The animators which affect this model part
    public final ArrayList<FiguraModelPart> children; // The children of this model part in the hierarchy tree, in iteration order
    private final Map<String, FiguraModelPart> childrenSpeedupCache = new WeakHashMap<>(); // Speedup cache for looking for children by name
    public final ArrayList<RenderTask<?>> renderTasks; // Render tasks act similar to children in some ways, such as inheriting transforms, but they are not FiguraModelPart.

    // Rendering
    public final @Nullable PartVertexData vertices; // Vertex data making up cubes/meshes of this part
//    public final byte[] vertices; // The vertices making up the cubes and meshes of the model part
    public @Nullable FiguraRenderType renderType; // The rendering policy of this part. Null to inherit unconditionally.
    public int renderTypePriority; // If the render type priority is >= than the parent's, renderType can replace the current set of render types.

    // Callbacks which are run during various stages of the rendering process.
    // TODO finalize the arg/return types for this!
    private ArrayList<ScriptCallback<CallbackItem.F32, CallbackItem.Unit>>
            preRenderCallbacks, midRenderCallbacks, postRenderCallbacks;

    // Alloc tracker state
    private final @Nullable AllocationTracker.State<AvatarOutOfMemoryError> allocState;

    // Construct a simple empty wrapper part around the given children
    public FiguraModelPart(String name, List<FiguraModelPart> children, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        this.name = name;
        this.transform = new PartTransform(allocationTracker);
        this.children = new ArrayList<>(children);
        this.renderTasks = new ArrayList<>(0);
        this.vertices = null;
        if (allocationTracker != null) {
            if (!name.isEmpty()) allocationTracker.track(name);
            int size = SIZE_ESTIMATE;
            size += children.size() * AllocationTracker.REFERENCE_SIZE;
            allocState = allocationTracker.track(this, size);
        } else allocState = null;
    }

    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 8
            + AllocationTracker.INT_SIZE;

    // Copy constructor.
    // Has many parameters for controlling what is deep-copied and what isn't.
    public FiguraModelPart(
            String newName,
            FiguraModelPart part,
            boolean deepCopyTransform,
            boolean deepCopyChildren, // Whether to deep-copy the children. These args will be propagated to those children as they're deep-copied.
            boolean deepCopyVertices,
            boolean deepCopyCallbackLists, // Whether to deep-copy the callback lists
            @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker
    ) throws AvatarOutOfMemoryError {
        this.name = newName;
        // Transform:
        this.transform = deepCopyTransform ? new PartTransform(part.transform, allocationTracker) : part.transform;
        // Children (But not the child list!)
        // This includes render tasks.
        this.children = new ArrayList<>(part.children.size());
        this.renderTasks = new ArrayList<>(part.renderTasks.size());
        for (var child : part.children)
            this.children.add(deepCopyChildren ? new FiguraModelPart(child.name, child, deepCopyTransform, true, deepCopyVertices, deepCopyCallbackLists, allocationTracker) : child);
        for (var task : part.renderTasks)
            this.renderTasks.add(deepCopyChildren ? task.copy(allocationTracker) : task);
        // Vertices:
        if (deepCopyVertices) {
            this.vertices = part.vertices == null ? null : new PartVertexData(part.vertices, allocationTracker);
        } else this.vertices = part.vertices;
        // Callback lists
        if (deepCopyCallbackLists) {
            this.preRenderCallbacks = part.preRenderCallbacks == null ? null : new ArrayList<>(part.preRenderCallbacks);
            this.midRenderCallbacks = part.midRenderCallbacks == null ? null : new ArrayList<>(part.midRenderCallbacks);
            this.postRenderCallbacks = part.postRenderCallbacks == null ? null : new ArrayList<>(part.postRenderCallbacks);
        } else {
            this.preRenderCallbacks = part.preRenderCallbacks;
            this.midRenderCallbacks = part.midRenderCallbacks;
            this.postRenderCallbacks = part.postRenderCallbacks;
        }
        // Copy other properties
        this.renderType = part.renderType;
        this.renderTypePriority = part.renderTypePriority;
        // Track this
        if (allocationTracker != null) {
            int size = SIZE_ESTIMATE;
            size += (
                    (this.preRenderCallbacks == null ? 0 : preRenderCallbacks.size())
                    + (this.midRenderCallbacks == null ? 0 : midRenderCallbacks.size())
                    + (this.postRenderCallbacks == null ? 0 : postRenderCallbacks.size())
                    + this.children.size()
            ) * AllocationTracker.REFERENCE_SIZE;
            allocState = allocationTracker.track(this, size);
        } else allocState = null;
    }

    // Vanilla parameter is used for mimics
    public FiguraModelPart(String name, AvatarModules.LoadTimeModule module, ModuleMaterials.ModelPartMaterials materials, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker, Textures texturesComponent, Materials materialsComponent, Molang molang, @Nullable VanillaRendering vanillaComponent) throws AvatarInitError, AvatarOutOfMemoryError {
        this.name = name;
        // If both zero, skip setting it
        transform = new PartTransform(allocationTracker);
        if (!materials.origin.equals(0,0,0) || !materials.rotation.equals(0,0,0)) {
            transform.setOrigin(materials.origin);
            transform.setEulerDeg(materials.rotation);
        }

        // Set up mimicry
        if (vanillaComponent != null && materials.mimic != null) {
            @Nullable VanillaRendering.ScriptVanillaPart part = vanillaComponent.nameToScriptPart.get(materials.mimic);
            if (part != null) {
                transform.setMimicPart(part);
            } else {
                // TODO emit a warning here since the part didn't exist
                transform.setVisible(false);
            }
        }

        // Get children
        children = MapUtils.<String, ModuleMaterials.ModelPartMaterials, FiguraModelPart, AvatarInitError, AvatarOutOfMemoryError>mapEntriesBiThrowing(materials.children, (childName, mat) -> switch (mat) {
            case ModuleMaterials.FigmodelMaterials figmodelMaterials -> new FigmodelModelPart(childName, module, figmodelMaterials, allocationTracker, texturesComponent, materialsComponent, molang, vanillaComponent);
            default -> new FiguraModelPart(childName, module, mat, allocationTracker, texturesComponent, materialsComponent, molang, vanillaComponent);
        });
        renderTasks = new ArrayList<>(0);

        // Get the render type:
        if (materials.materialIndex != null) {
            // If material index is not null, then get the render type
            renderType = materialsComponent.getMaterial(module.index, materials.materialIndex);
        } else if (!children.isEmpty()) {
            // Otherwise, attempt to merge from children:
            boolean sameRenderType = children.stream().map(p -> p.renderType).filter(Objects::nonNull).distinct().limit(2).count() <= 1;
            if (sameRenderType) {
                // If all children have the same render type, merge upwards,
                // setting their render types to null and this render type to that one.
                this.renderType = children.getFirst().renderType;
                for (FiguraModelPart child : children) child.renderType = null;
            }
        }

        // Get vertices
        PartVertexData.Builder verticesBuilder = PartVertexData.builder(); // TODO fix this and make it use unboxed floats
        for (ModuleMaterials.CubeData cubeData : materials.cubes) addVertices(verticesBuilder, cubeData);
        for (ModuleMaterials.MeshData meshData : materials.meshes) addVertices(verticesBuilder, meshData);
        vertices = verticesBuilder.build(allocationTracker);

        // Register to alloc tracker
        if (allocationTracker != null) {
            // Track this
            int size = SIZE_ESTIMATE;
            size += children.size() * AllocationTracker.REFERENCE_SIZE;
            allocState = allocationTracker.track(this, size);
        } else allocState = null;
    }

    // Construct by extruding a texture
    public FiguraModelPart(String name, AvatarTexture texture, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        this.name = name;
        this.transform = new PartTransform(allocationTracker);
        this.renderType = FiguraRenderType.basic(0, texture, null, null, null);
        this.children = new ArrayList<>(0);
        this.renderTasks = new ArrayList<>(0);
        PartVertexData.Builder builder = PartVertexData.builder();
        // Iterate in each direction!
        byte[] opacityStates = new byte[Math.max(texture.getWidth(), texture.getHeight()) + 2]; // +2 because of 1 pixel padding on each side
        int w = texture.getWidth();
        int h = texture.getHeight();

        // Iterations run with -1 and <= in order to algorithmically "pad" the image with +1 pixel on each side of emptiness, making the algorithm a bit simpler.

        // Horizontal sweep with vertical scanline
        for (int x = -1; x <= w; x++) {
            // "Building state" indicates our current progress in creating a quad as we scan downwards.
            // As we scan down the second column, imagine a group of pixels like this:
            // ■ ■
            // ■|
            // ■|
            //  |■
            // ■|
            // ■ ■
            // We're going to need to emit those four "|" characters as quads.
            // Quads 1, 2, and 4 will be facing right, and quad 3 will be facing left.
            // Also, as an optimization, we will merge 1 and 2, so actually only 3 quads will be created.
            // Let's track building state, row by row:
            // Row 1: opacityState == 2, and prevOpacityState == 2, so newBuildingState is 0. Since buildingState starts as 0, no quad is created.
            // Row 2: opacityState == 0, and prevOpacityState == 2, so newBuildingState is -1. This means that we're going to start a quad facing right.
            // Row 3: opacityState == 0, and prevOpacityState == 2, so newBuildingState is -1. This is the same as before, so we do nothing, and continue the existing quad.
            // Row 4: opacityState == 2, and prevOpacityState == 0, so newBuildingState is  1. Since this is different, we will end our existing quad facing right, and begin a new quad facing left.
            // Row 5: opacityState == 0, and prevOpacityState == 2, so newBuildingState is -1. Since this is different, we will end our existing quad facing left, and begin a new quad facing right.
            // Row 6: opacityState == 2, and prevOpacityState == 2, so newBuildingState is 0. Since this is different, we will end our existing quad facing right.
            // We've now begun and ended the three quads for this column, and prepared the "opacityStates" for the next column, so we repeat!
            float buildingState = 0;
            for (int y = -1; y <= h; y++) {
                byte opacityState = (x < 0 || y < 0 || x >= w || y >= h) ? 0 : getOpacityState(texture, x, y);
                if (x >= 0) {
                    byte prevOpacityState = opacityStates[y+1]; // This is the opacity state of the pixel to the left!
                    float newBuildingState = Math.signum(opacityState - prevOpacityState);
                    if (buildingState != newBuildingState) {
                        // We're either starting a quad, ending one, or both.
                        // If we go from anything to 0, we're ending a quad.
                        // If we go from 0 to anything, we're starting a quad.
                        // If we go from 1 to -1 or vice versa, we're ending a quad and starting a quad at once.
                        if (buildingState != 0) {
                            // We're ending a quad
                            noSkinVert(builder, x, h - y, (buildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, -1f * buildingState, 0f, 0f, 0f, 0f, newBuildingState, null, null);
                            noSkinVert(builder, x, h - y, (buildingState + 1) / 2, (x + buildingState) / w, (y + 0f) / h, -1f * buildingState, 0f, 0f, 0f, 0f, newBuildingState, null, null);
                        }
                        if (newBuildingState != 0) {
                            // We're starting a quad
                            noSkinVert(builder, x, h - y, (newBuildingState + 1) / 2, (x + newBuildingState) / w, (y + 0f) / h, -1f * newBuildingState, 0f, 0f, 0f, 0f, newBuildingState, null, null);
                            noSkinVert(builder, x, h - y, (newBuildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, -1f * newBuildingState, 0f, 0f, 0f, 0f, newBuildingState, null, null);
                        }
                    }
                    buildingState = newBuildingState;
                }
                opacityStates[y+1] = opacityState;
            }
            if (buildingState != 0) throw new IllegalStateException("Failed to extrude texture? Internal bug in Figura, please report!");
        }

        // Vertical sweep with horizontal scanline.
        // This is largely the same as the above loop, just flip the axes.
        for (int y = -1; y <= h; y++) {
            float buildingState = 0;
            for (int x = -1; x <= w; x++) {
                byte opacityState = (x < 0 || y < 0 || x >= w || y >= h) ? 0 : getOpacityState(texture, x, y);
                if (y >= 0) {
                    byte prevOpacityState = opacityStates[x+1];
                    float newBuildingState = Math.signum(opacityState - prevOpacityState);
                    if (buildingState != newBuildingState) {
                        // We're either starting a quad, ending one, or both.
                        if (buildingState != 0) {
                            // We're ending a quad
                            noSkinVert(builder, x, h - y, (buildingState + 1) / 2, (x + 0f) / w, (y + buildingState) / h, 0f, -1f * buildingState, 0f, 1f, 0f, 0f, null, null);
                            noSkinVert(builder, x, h - y, (buildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, 0f, -1f * buildingState, 0f, 1f, 0f, 0f, null, null);
                        }
                        if (newBuildingState != 0) {
                            // We're starting a quad
                            noSkinVert(builder, x, h - y, (newBuildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, 0f, -1f * newBuildingState, 0f, 1f, 0f, 0f, null, null);
                            noSkinVert(builder, x, h - y, (newBuildingState + 1) / 2, (x + 0f) / w, (y + newBuildingState) / h, 0f, -1f * newBuildingState, 0f, 1f, 0f, 0f, null, null);
                        }
                    }
                    buildingState = newBuildingState;
                }
                opacityStates[x+1] = opacityState;
            }
            if (buildingState != 0) throw new IllegalStateException("Failed to extrude texture? Internal bug in Figura, please report!");
        }

        // Put on the front and back panels
        // TODO: if this has issues with transparency or something, replace it with multiple quads that only cover the actual filled pixels
        noSkinVert(builder, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, null, null);
        noSkinVert(builder, w, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, null, null);
        noSkinVert(builder, w, h, 1f, 1f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, null, null);
        noSkinVert(builder, 0f, h, 1f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, null, null);

        noSkinVert(builder, w, 0f, 0f, 1f, 1f, 0f, 0f, -1f, -1f, 0f, 0f, null, null);
        noSkinVert(builder, 0f, 0f, 0f, 0f, 1f, 0f, 0f, -1f, -1f, 0f, 0f, null, null);
        noSkinVert(builder, 0f, h, 0f, 0f, 0f, 0f, 0f, -1f, -1f, 0f, 0f, null, null);
        noSkinVert(builder, w, h, 0f, 1f, 0f, 0f, 0f, -1f, -1f, 0f, 0f, null, null);

        vertices = builder.build(allocationTracker);

        // Track
        if (allocationTracker != null) {
            allocState = allocationTracker.track(this, SIZE_ESTIMATE);
        } else allocState = null;
    }

    // Return the "opacity state" for the pixel at (x, y)
    // For a fully transparent pixel with alpha 0, opacityState = 0.
    // For a partially transparent pixel with alpha 1 to 254, opacityState = 1.
    // For a fully opaque pixel with alpha 255, opacityState = 2.
    // Assumes x,y is in bounds for the image!
    private static byte getOpacityState(AvatarTexture texture, int x, int y) {
        return (byte) (((texture.getPixel(x, y) >>> 24) + 253) / 254);
    }

    private static void addVertices(PartVertexData.Builder vertexData, ModuleMaterials.CubeData cubeData) {
        Vector3f f = cubeData.from().sub(cubeData.inflate(), new Vector3f());
        Vector3f t = cubeData.to().add(cubeData.inflate(), new Vector3f());
        Vector3f o = cubeData.origin();
        Vector3f r = cubeData.rotation();

        // Rotate around its origin:
        Matrix4f transform = new Matrix4f()
                .translate(o.x, o.y, o.z)
                .rotate(new Quaternionf().rotationZYX(r.z * MathUtils.DEG_TO_RAD, r.y * MathUtils.DEG_TO_RAD, r.x * MathUtils.DEG_TO_RAD))
                .translate(-o.x, -o.y, -o.z)
        ;
        Matrix3f normalMat = transform.normal(new Matrix3f());

        for (int i = 0; i < 6; i++) {
            @Nullable ModuleMaterials.CubeFace face = cubeData.faces()[i];
            if (face == null) continue;
            float u1 = face.uv().x();
            float v1 = face.uv().y();
            float u2 = face.uv().z();
            float v2 = face.uv().y();
            float u3 = face.uv().z();
            float v3 = face.uv().w();
            float u4 = face.uv().x();
            float v4 = face.uv().w();
            int faceRot = face.rot();
            while (faceRot > 0) { //rotate texture
                float temp = u1;
                u1 = u2; u2 = u3; u3 = u4; u4 = temp;
                temp = v1;
                v1 = v2; v2 = v3; v3 = v4; v4 = temp;
                faceRot--;
            }
            switch (i) {
                case 0 -> { // West (Negative X)
                    noSkinVert(vertexData, f.x, f.y, f.z, u4, v4, -1f, 0f, 0f, 0f, 0f, 1f, transform, normalMat);
                    noSkinVert(vertexData, f.x, f.y, t.z, u3, v3, -1f, 0f, 0f, 0f, 0f, 1f, transform, normalMat);
                    noSkinVert(vertexData, f.x, t.y, t.z, u2, v2, -1f, 0f, 0f, 0f, 0f, 1f, transform, normalMat);
                    noSkinVert(vertexData, f.x, t.y, f.z, u1, v1, -1f, 0f, 0f, 0f, 0f, 1f, transform, normalMat);
                }
                case 1 -> { // East (Positive X)
                    noSkinVert(vertexData, t.x, f.y, t.z, u4, v4, 1f, 0f, 0f, 0f, 0f, -1f, transform, normalMat);
                    noSkinVert(vertexData, t.x, f.y, f.z, u3, v3, 1f, 0f, 0f, 0f, 0f, -1f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, f.z, u2, v2, 1f, 0f, 0f, 0f, 0f, -1f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, t.z, u1, v1, 1f, 0f, 0f, 0f, 0f, -1f, transform, normalMat);
                }
                case 2 -> { // Down (Negative Y)
                    noSkinVert(vertexData, f.x, f.y, f.z, u4, v4, 0f, -1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, f.y, f.z, u3, v3, 0f, -1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, f.y, t.z, u2, v2, 0f, -1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, f.x, f.y, t.z, u1, v1, 0f, -1f, 0f, 1f, 0f, 0f, transform, normalMat);
                }
                case 3 -> { // Up (Positive Y)
                    noSkinVert(vertexData, f.x, t.y, t.z, u4, v4, 0f, 1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, t.z, u3, v3, 0f, 1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, f.z, u2, v2, 0f, 1f, 0f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, f.x, t.y, f.z, u1, v1, 0f, 1f, 0f, 1f, 0f, 0f, transform, normalMat);
                }
                case 4 -> { // North (Negative Z)
                    noSkinVert(vertexData, t.x, f.y, f.z, u4, v4, 0f, 0f, -1f, -1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, f.x, f.y, f.z, u3, v3, 0f, 0f, -1f, -1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, f.x, t.y, f.z, u2, v2, 0f, 0f, -1f, -1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, f.z, u1, v1, 0f, 0f, -1f, -1f, 0f, 0f, transform, normalMat);
                }
                case 5 -> { // South (Positive Z)
                    noSkinVert(vertexData, f.x, f.y, t.z, u4, v4, 0f, 0f, 1f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, f.y, t.z, u3, v3, 0f, 0f, 1f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, t.x, t.y, t.z, u2, v2, 0f, 0f, 1f, 1f, 0f, 0f, transform, normalMat);
                    noSkinVert(vertexData, f.x, t.y, t.z, u1, v1, 0f, 0f, 1f, 1f, 0f, 0f, transform, normalMat);
                }
            }
        }
    }

    private static void addVertices(PartVertexData.Builder builder, ModuleMaterials.MeshData meshData) {

        // Rotate around its origin:
        Vector3f o = meshData.origin();
        Vector3f r = meshData.rotation();
        Matrix4f transform = new Matrix4f()
                .translate(o.x, o.y, o.z)
                .rotate(new Quaternionf().rotationXYZ(r.x * MathUtils.DEG_TO_RAD, r.y * MathUtils.DEG_TO_RAD, r.z * MathUtils.DEG_TO_RAD)) // Meshes use XYZ rotation order! This is different from other part types!
                // .translate(-o.x, -o.y, -o.z) // Meshes use their origins as translations, unlike cubes which use them only as pivot points, which is why this is commented out!
        ;
        Matrix3f normalMat = transform.normal(new Matrix3f());

        // Create the faces...
        List<ModuleMaterials.VertexData> vertices = meshData.vertices();
        List<Vector2f> uvs = meshData.uvs();
        int uv = 0;
        for (Vector4i face : meshData.indices()) {
            // Always do 3 vertices
            ModuleMaterials.VertexData v1 = vertices.get(face.x);
            ModuleMaterials.VertexData v2 = vertices.get(face.y);
            ModuleMaterials.VertexData v3 = vertices.get(face.z);
            Vector3f normal = computeNormal(v1.pos(), v2.pos(), v3.pos());
            meshVert(builder, v1, normal, uvs.get(uv++), transform, normalMat);
            meshVert(builder, v2, normal, uvs.get(uv++), transform, normalMat);
            meshVert(builder, v3, normal, uvs.get(uv++), transform, normalMat);
            if (face.w != -1) {
                // This is a quad, add the 4th vertex
                ModuleMaterials.VertexData v4 = vertices.get(face.w);
                meshVert(builder, v4, normal, uvs.get(uv++), transform, normalMat);
            } else {
                // This is a triangle but minecraft likes quads, so emit the 3rd vertex again
                meshVert(builder, v3, normal, uvs.get(uv - 1), transform, normalMat);
            }
        }
    }

    private static Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        return v2.sub(v1, new Vector3f()).cross(v3.sub(v1, new Vector3f())).normalize();
    }

    private static void meshVert(PartVertexData.Builder builder, ModuleMaterials.VertexData vertexData, Vector3f normalVec, Vector2f uv, Matrix4f transform, Matrix3f normalMat) {
        Vector3f p = vertexData.pos();
        if (vertexData.skinningData() == null) {
            emitVert(builder,
                    p.x, p.y, p.z,
                    uv.x, uv.y,
                    normalVec.x, normalVec.y, normalVec.z,
                    0, 0, 0, // TODO mesh tangent vectors??
                    0, -1, -1, -1,
                    1f, 0f, 0f, 0f,
                    transform, normalMat
            );
        } else {
            Vector4i so = vertexData.skinningData().offsets();
            Vector4f sw = vertexData.skinningData().weights();
            emitVert(builder,
                    p.x, p.y, p.z,
                    uv.x, uv.y,
                    normalVec.x, normalVec.y, normalVec.z,
                    0, 0, 0, // TODO mesh tangent vectors??
                    so.x, so.y, so.z, so.w,
                    sw.x, sw.y, sw.z, sw.w,
                    transform, normalMat
            );
        }
    }

    private static void noSkinVert(PartVertexData.Builder builder, float x, float y, float z, float u, float v, float nx, float ny, float nz, float tx, float ty, float tz, @Nullable Matrix4f transform, @Nullable Matrix3f normalMat) {
        emitVert(builder, x, y, z, u, v, nx, ny, nz, tx, ty, tz, 0, -1, -1, -1, 1f, 0f, 0f, 0f, transform, normalMat);
    }

    private static void emitVert(
            PartVertexData.Builder builder,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz, // Normal
            float tx, float ty, float tz, // Tangent
            int riggingOffset0, int riggingOffset1, int riggingOffset2, int riggingOffset3,
            float riggingWeight0, float riggingWeight1, float riggingWeight2, float riggingWeight3,
            @Nullable Matrix4f transform, @Nullable Matrix3f normalMat
    ) {
        // Calc pos and normal
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f tangent = new Vector3f(tx, ty, tz);
        if (transform != null) {
            pos.mulPosition(transform);
            tangent.mulDirection(transform);
        }
        Vector3f norm = new Vector3f(nx, ny, nz);
        if (normalMat != null) norm.mul(normalMat);
        norm.normalize();
        // Build vertex
        builder.position(pos.x, pos.y, pos.z)
                .riggingWeights(riggingWeight0, riggingWeight1, riggingWeight2, riggingWeight3)
                .riggingOffsets((byte) (riggingOffset0 & 0xFF), (byte) (riggingOffset1 & 0xFF), (byte) (riggingOffset2 & 0xFF), (byte) (riggingOffset3 & 0xFF))
                .uv(u, v)
                .normal(norm.x, norm.y, norm.z)
                .tangent(tangent.x, tangent.y, tangent.z)
                .endVertex();
    }

    // Script-y functions

    public void addChild(FiguraModelPart child) throws AvatarOutOfMemoryError {
        if (this.allocState != null) this.allocState.changeSize(AllocationTracker.REFERENCE_SIZE);
        QueuedSetters.handle(() -> this.children.add(child));
    }

    public void removeChild(FiguraModelPart child) {
        QueuedSetters.handle(() -> {
            this.children.remove(child);
            this.childrenSpeedupCache.remove(child.name);
        });
    }

    public void addRenderTask(RenderTask<?> task) throws AvatarOutOfMemoryError {
        if (this.allocState != null) this.allocState.changeSize(AllocationTracker.REFERENCE_SIZE);
        QueuedSetters.handle(() -> this.renderTasks.add(task));
    }


    @Override
    public PartTransform getTransform() {
        return transform;
    }

    @Override
    public @Nullable FiguraModelPart getChildByName(String name) {
        FiguraModelPart cached = childrenSpeedupCache.get(name);
        if (cached != null) return cached;
        for (FiguraModelPart part : children) {
            if (part.name.equals(name)) {
                cached = part; break;
            }
        }
        if (cached != null) childrenSpeedupCache.put(name, cached);
        return cached;
    }


}
