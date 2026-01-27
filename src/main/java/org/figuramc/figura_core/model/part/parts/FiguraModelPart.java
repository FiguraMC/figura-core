package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.Materials;
import org.figuramc.figura_core.avatars.components.RenderDataHolder;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.part.PartTransform;
import org.figuramc.figura_core.model.part.RiggedHierarchy;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.model.rendering.RenderData;
import org.figuramc.figura_core.model.rendering.vertex.PartVertexData;
import org.figuramc.figura_core.model.rendering.vertex.VertexDataExtraction;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    // Rendering
    public @Nullable PartVertexData vertices; // Vertex data making up cubes/meshes of this part (not yet baked)
    public @Nullable FiguraRenderType renderType; // Render type for this part. Pass null to inherit unconditionally.
    public int renderTypePriority; // If the render type priority is >= than the parent's, renderType can replace the current render type (TODO is this actually helpful?)
    public @Nullable RenderData renderData; // Contains information needed for rendering this part as a root

    // The avatar who owns this part.
    // Should only be used in a thread-safe way, since it's not behind an exclusive AvatarView!!
    protected final Avatar<?> owningAvatar;
    // Alloc tracker state
    private final @Nullable AllocationTracker.State<AvatarOutOfMemoryError> allocState;

    // Construct a simple empty wrapper part around the given children
    public FiguraModelPart(Avatar<?> owningAvatar, String name, List<FiguraModelPart> children) throws AvatarOutOfMemoryError {
        this.owningAvatar = owningAvatar;
        this.name = name;
        this.transform = new PartTransform(owningAvatar.allocationTracker);
        this.children = new ArrayList<>(children);
        this.vertices = null;
        if (owningAvatar.allocationTracker != null) {
            if (!name.isEmpty()) owningAvatar.allocationTracker.track(name);
            int size = SIZE_ESTIMATE;
            size += children.size() * AllocationTracker.REFERENCE_SIZE;
            allocState = owningAvatar.allocationTracker.track(this, size);
        } else allocState = null;
    }

    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 8
            + AllocationTracker.INT_SIZE;

    // Construct from materials. Vanilla rendering parameter is used for mimics
    public FiguraModelPart(Avatar<?> owningAvatar, String name, AvatarModules.LoadTimeModule module, ModuleMaterials.ModelPartMaterials materials) throws AvatarInitError, AvatarOutOfMemoryError {
        this.owningAvatar = owningAvatar;
        this.name = name;
        // Get required and optional components
        Materials materialsComponent = owningAvatar.assertComponent(Materials.TYPE);
        VanillaRendering vanillaComponent = owningAvatar.getComponent(VanillaRendering.TYPE);

        // If both zero, skip setting it
        transform = new PartTransform(owningAvatar.allocationTracker);
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
            case ModuleMaterials.FigmodelMaterials figmodelMaterials -> new FigmodelModelPart(owningAvatar, childName, module, figmodelMaterials);
            default -> new FiguraModelPart(owningAvatar, childName, module, mat);
        });

        // Get the render type
        if (materials.materialIndex != null) {
            // If material index is not null, then get the render type
            renderType = materialsComponent.getMaterial(module.index, materials.materialIndex);
            // Also get vertices
            PartVertexData.Builder verticesBuilder = PartVertexData.builder();
            for (ModuleMaterials.CubeData cubeData : materials.cubes) VertexDataExtraction.addVertices(verticesBuilder, cubeData);
            for (ModuleMaterials.MeshData meshData : materials.meshes) VertexDataExtraction.addVertices(verticesBuilder, meshData);
            vertices = verticesBuilder.build(owningAvatar.allocationTracker);
        }

        // Register to alloc tracker
        if (owningAvatar.allocationTracker != null) {
            // Track this
            int size = SIZE_ESTIMATE;
            size += children.size() * AllocationTracker.REFERENCE_SIZE;
            allocState = owningAvatar.allocationTracker.track(this, size);
        } else allocState = null;
    }

    // Construct by extruding a texture (TODO: normal/specular buddies?)
    public FiguraModelPart(Avatar<?> owningAvatar, String name, AvatarTexture texture) throws AvatarOutOfMemoryError {
        this.owningAvatar = owningAvatar;
        this.name = name;
        this.transform = new PartTransform(owningAvatar.allocationTracker);
        this.renderType = FiguraRenderType.albedo(0, new FiguraRenderType.TextureBinding(texture.getHandle(), texture.getUvValues()));
        this.children = new ArrayList<>(0);
        PartVertexData.Builder builder = PartVertexData.builder();
        VertexDataExtraction.extrudeTexture(builder, texture);
        vertices = builder.build(owningAvatar.allocationTracker);
        // Track
        if (owningAvatar.allocationTracker != null) {
            allocState = owningAvatar.allocationTracker.track(this, SIZE_ESTIMATE);
        } else allocState = null;
    }

    // Rendering stuff

    // Whether this part functions as a rendering root
    protected boolean isRenderingRoot() {
        return this.renderData != null;
    }

    // Trigger a vertex rebuild of this part. Stores resulting data in the associated RenderData.
    public void buildRenderingData() throws AvatarError, AvatarOutOfMemoryError {
        RenderData.Builder builder = RenderData.builder();
        buildRenderingData(builder, this.renderType, this.renderTypePriority);
        this.renderData = builder.build(owningAvatar.getComponent(RenderDataHolder.TYPE), owningAvatar.allocationTracker);
    }
    // If a child has its own separate managed rendering data, don't recurse into it
    private void buildRenderingData(RenderData.Builder out, FiguraRenderType renderType, int renderTypePriority) throws AvatarError {
        // Update render type and priority if needed
        if (this.renderType != null && this.renderTypePriority >= renderTypePriority) {
            renderType = this.renderType;
            renderTypePriority = this.renderTypePriority;
        }
        // Notify the builder this part exists
        if (this.vertices != null) {
            out.addModelPart(this.vertices, renderType);
        }
        // Recurse on children
        for (FiguraModelPart child : this.children) {
            // Only recurse if the child doesn't have its own managed rendering data
            if (child.renderData == null) {
                child.buildRenderingData(out, renderType, renderTypePriority);
            }
        }
    }

    // Traverse this part, gathering transformations, and draw.
    // Additionally, if the part count has changed for a child part, this part must have been since rebuilt;
    // Otherwise, the depth-first model part indices could become incorrect.
    // State is passthrough to all ClientPartRenderer impls.
    public void render(FiguraTransformStack transformStack, Object state) throws AvatarError, AvatarOutOfMemoryError {
        RenderData renderData = this.renderData;
        this.renderHelper(transformStack, true, 0, renderData, state);
        if (renderData != null) renderData.clientPartRenderer.draw(transformStack, state);
    }
    // Returns new part ID
    private int renderHelper(
            FiguraTransformStack transformStack,
            boolean currentlyVisible,
            int partID,
            @Nullable RenderData renderData, // If data is null, we don't emit any transforms, just proceed to find descendants who do have RenderData
            Object state
    ) throws AvatarError, AvatarOutOfMemoryError {
        // Update transform stack and visibility
        currentlyVisible = currentlyVisible && transform.getVisible();
        transformStack.push();
        if (currentlyVisible) transform.affect(transformStack);
        // Only parts with vertices use IDs
        if (vertices != null && renderData != null)
            renderData.partData[partID++].fillFromStack(transformStack, currentlyVisible);
        // Recurse on children
        for (FiguraModelPart child : children) {
            if (child.isRenderingRoot()) {
                // If the child has their own separate render data, and we're visible, recurse with the *original* render function, drawing a whole new tree.
                if (currentlyVisible) child.render(transformStack, state);
            } else {
                // Otherwise, continue recursing in this helper, and update the part ID.
                partID = child.renderHelper(transformStack, currentlyVisible, partID, renderData, state);
            }
        }
        // Pop and return
        transformStack.pop();
        return partID;
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
