package org.figuramc.figura_core.model.part;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.data.ModuleMaterials;
import org.figuramc.figura_core.model.shader.FiguraRenderType;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;

/**
 * Corresponds to a Group in Blockbench.
 * <p>
 * Unlike previously, Figura's scripting no longer allows manipulation of individual cubes and meshes, only groups.
 * Here's why:
 * - This is more in-line with Blockbench, as Blockbench only allows animations to affect groups
 * - Also more in-line with Minecraft's internals, because Vanilla model parts do not have individual cube transforms
 * - This can be more efficient rendering-wise, because most of the time individual cubes are not articulated, allowing
 *   for less unneeded computation. When they do need to be articulated, one can simply add a group for said cube.
 */
public class FiguraModelPart implements RiggedHierarchy<FiguraModelPart> {

    // Structure / modifications
    public final PartTransform transform; // The transform of this model part

//    private  animators; // The animators which affect this model part
    public final LinkedHashMap<String, FiguraModelPart> children; // The children of this model part in the hierarchy tree. Ordered, so we don't use a regular hashmap.

    // Rendering
    public final float[] vertices; // The vertices making up the cubes and meshes of the model part
    private @Nullable FiguraRenderType renderType; // The rendering policy of this part. Null to inherit unconditionally.
    public int renderTypePriority; // If the render type priority is higher than the parent's, renderType can replace the current render types.

    // Callbacks which are run during various stages of the rendering process.
    // TODO finalize the arg/return types for this!
    public final ArrayList<ScriptCallback<CallbackItem.F32, CallbackItem.Unit>>
            preRenderCallbacks = new ArrayList<>(0), // Zero sized at first, since most parts will not have callbacks
            midRenderCallbacks = new ArrayList<>(0),
            postRenderCallbacks = new ArrayList<>(0);

    // Alloc tracker state
    private final @Nullable AllocationTracker.State<AvatarError> allocState;

    // Construct a simple empty wrapper part around the given children
    public FiguraModelPart(Map<String, FiguraModelPart> children, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        this.transform = new PartTransform(allocationTracker);
        this.children = new LinkedHashMap<>(children);
        this.vertices = new float[0];
        if (allocationTracker != null) {
            for (var key : children.keySet())
                allocationTracker.track(key);
            allocState = allocationTracker.track(this, SIZE_ESTIMATE);
        } else allocState = null;
    }

    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 8
            + AllocationTracker.INT_SIZE;

    // Vanilla parameter is used for mimics
    public FiguraModelPart(AvatarModules.LoadTimeModule module, ModuleMaterials.ModelPartMaterials materials, @Nullable AllocationTracker<AvatarError> allocationTracker, Textures texturesComponent, Molang molang, @Nullable VanillaRendering vanillaComponent) throws AvatarError {
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
        children = MapUtils.mapValues(materials.children, (name, mat) -> switch (mat) {
            case ModuleMaterials.FigmodelMaterials figmodelMaterials -> new FigmodelModelPart(module, name, figmodelMaterials, allocationTracker, texturesComponent, molang, vanillaComponent);
            default -> new FiguraModelPart(module, mat, allocationTracker, texturesComponent, molang, vanillaComponent);
        }, LinkedHashMap::new);

        // Get the list of render types:
        Vector4f uvModifier = new Vector4f(0, 0, 1, 1);
        if (materials.textureIndex != -1) {
            // If tex index is not -1, then generate a render type from the texture:
            AvatarTexture tex = texturesComponent.getTexture(module.index, materials.textureIndex);
            renderType = new FiguraRenderType.Basic(tex.getHandle(), null);
            // Also, set the UV modifier from the texture (for atlases)
            uvModifier.set(tex.getUvValues());
        } else if (!children.isEmpty()) {
            // Otherwise, attempt to merge from children:
            boolean sameRenderType = children.values().stream().map(FiguraModelPart::getRenderType).filter(Objects::nonNull).distinct().limit(2).count() <= 1;
            if (sameRenderType) {
                // If all children have the same render type, merge upwards,
                // setting their render types to null and this render type to that one.
                this.renderType = children.firstEntry().getValue().getRenderType();
                for (FiguraModelPart child : children.values())
                    child.renderType = null;
            }
        }

        // Get vertices
        List<Float> vertexData = new ArrayList<>(); // TODO fix this and make it use unboxed floats
        for (ModuleMaterials.CubeData cubeData : materials.cubes) addVertices(vertexData, cubeData, uvModifier);
        for (ModuleMaterials.MeshData meshData : materials.meshes) addVertices(vertexData, meshData, uvModifier);
        vertices = new float[vertexData.size()];
        for (int i = 0; i < vertexData.size(); i++) vertices[i] = vertexData.get(i);

        // Register to alloc tracker
        if (allocationTracker != null) {
            // Track vertices
            if (vertices.length > 0) allocationTracker.track(vertices);
            // Track string children names
            for (var key : children.keySet())
                allocationTracker.track(key);
            // Track this
            allocState = allocationTracker.track(this, SIZE_ESTIMATE);
        } else allocState = null;
    }

    // Construct by extruding a texture
    public FiguraModelPart(AvatarTexture texture, @Nullable AllocationTracker<AvatarError> allocationTracker) throws AvatarError {
        this.transform = new PartTransform(allocationTracker);
        this.renderType = new FiguraRenderType.Basic(texture.getHandle(), null);
        Vector4f uvModifier = texture.getUvValues();
        this.children = new LinkedHashMap<>();
        List<Float> vertexData = new ArrayList<>();
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
                            noSkinVert(vertexData, x, h - y, (buildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, -1f * buildingState, 0f, 0f, null, null, uvModifier);
                            noSkinVert(vertexData, x, h - y, (buildingState + 1) / 2, (x + buildingState) / w, (y + 0f) / h, -1f * buildingState, 0f, 0f, null, null, uvModifier);
                        }
                        if (newBuildingState != 0) {
                            // We're starting a quad
                            noSkinVert(vertexData, x, h - y, (newBuildingState + 1) / 2, (x + newBuildingState) / w, (y + 0f) / h, -1f * newBuildingState, 0f, 0f, null, null, uvModifier);
                            noSkinVert(vertexData, x, h - y, (newBuildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, -1f * newBuildingState, 0f, 0f, null, null, uvModifier);
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
                            noSkinVert(vertexData, x, h - y, (buildingState + 1) / 2, (x + 0f) / w, (y + buildingState) / h, 0f, -1f * buildingState, 0f, null, null, uvModifier);
                            noSkinVert(vertexData, x, h - y, (buildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, 0f, -1f * buildingState, 0f, null, null, uvModifier);
                        }
                        if (newBuildingState != 0) {
                            // We're starting a quad
                            noSkinVert(vertexData, x, h - y, (newBuildingState - 1) / -2, (x + 0f) / w, (y + 0f) / h, 0f, -1f * newBuildingState, 0f, null, null, uvModifier);
                            noSkinVert(vertexData, x, h - y, (newBuildingState + 1) / 2, (x + 0f) / w, (y + newBuildingState) / h, 0f, -1f * newBuildingState, 0f, null, null, uvModifier);
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
        noSkinVert(vertexData, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 1f, null, null, uvModifier);
        noSkinVert(vertexData, w, 0f, 1f, 1f, 1f, 0f, 0f, 1f, null, null, uvModifier);
        noSkinVert(vertexData, w, h, 1f, 1f, 0f, 0f, 0f, 1f, null, null, uvModifier);
        noSkinVert(vertexData, 0f, h, 1f, 0f, 0f, 0f, 0f, 1f, null, null, uvModifier);

        noSkinVert(vertexData, w, 0f, 0f, 1f, 1f, 0f, 0f, -1f, null, null, uvModifier);
        noSkinVert(vertexData, 0f, 0f, 0f, 0f, 1f, 0f, 0f, -1f, null, null, uvModifier);
        noSkinVert(vertexData, 0f, h, 0f, 0f, 0f, 0f, 0f, -1f, null, null, uvModifier);
        noSkinVert(vertexData, w, h, 0f, 1f, 0f, 0f, 0f, -1f, null, null, uvModifier);

        vertices = new float[vertexData.size()];
        for (int i = 0; i < vertexData.size(); i++) vertices[i] = vertexData.get(i);

        // Set up transform to be item-ish
        transform.setScale(1f/16);
        transform.setOrigin(0f, 0f, 7.5f);

        // Track
        if (allocationTracker != null) {
            allocationTracker.track(vertices);
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

    private static void addVertices(List<Float> vertexData, ModuleMaterials.CubeData cubeData, Vector4f uvModifier) {
        Vector3f f = cubeData.from().sub(cubeData.inflate(), new Vector3f());
        Vector3f t = cubeData.to().add(cubeData.inflate(), new Vector3f());
        Vector3f o = cubeData.origin();
        Vector3f r = cubeData.rotation();

        // Scale down by 1/16 and rotate around its origin.
        Matrix4f transform = new Matrix4f()
                .scale(1.0f / 16)
                .translate(o.x, o.y, o.z)
                .rotate(new Quaternionf().rotationZYX(r.z * MathUtils.DEG_TO_RAD, r.y * MathUtils.DEG_TO_RAD, r.x * MathUtils.DEG_TO_RAD))
                .translate(-o.x, -o.y, -o.z)
        ;

        // Also scale normal matrix to compensate
        Matrix3f normalMat = transform.normal(new Matrix3f()).scale(1.0f / 16);

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
                    noSkinVert(vertexData, f.x, f.y, f.z, u4, v4, -1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, f.y, t.z, u3, v3, -1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, t.y, t.z, u2, v2, -1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, t.y, f.z, u1, v1, -1f, 0f, 0f, transform, normalMat, uvModifier);
                }
                case 1 -> { // East (Positive X)
                    noSkinVert(vertexData, t.x, f.y, t.z, u4, v4, 1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, f.y, f.z, u3, v3, 1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, f.z, u2, v2,1f, 0f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, t.z, u1, v1, 1f, 0f, 0f, transform, normalMat, uvModifier);
                }
                case 2 -> { // Down (Negative Y)
                    noSkinVert(vertexData, f.x, f.y, f.z, u4, v4, 0f, -1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, f.y, f.z, u3, v3, 0f, -1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, f.y, t.z, u2, v2, 0f, -1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, f.y, t.z, u1, v1, 0f, -1f, 0f, transform, normalMat, uvModifier);
                }
                case 3 -> { // Up (Positive Y)
                    noSkinVert(vertexData, f.x, t.y, t.z, u4, v4, 0f, 1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, t.z, u3, v3, 0f, 1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, f.z, u2, v2, 0f, 1f, 0f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, t.y, f.z, u1, v1, 0f, 1f, 0f, transform, normalMat, uvModifier);
                }
                case 4 -> { // North (Negative Z)
                    noSkinVert(vertexData, t.x, f.y, f.z, u4, v4, 0f, 0f, -1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, f.y, f.z, u3, v3, 0f, 0f, -1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, t.y, f.z, u2, v2,0f, 0f, -1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, f.z, u1, v1, 0f, 0f, -1f, transform, normalMat, uvModifier);
                }
                case 5 -> { // South (Positive Z)
                    noSkinVert(vertexData, f.x, f.y, t.z, u4, v4, 0f, 0f, 1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, f.y, t.z, u3, v3, 0f, 0f, 1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, t.x, t.y, t.z, u2, v2, 0f, 0f, 1f, transform, normalMat, uvModifier);
                    noSkinVert(vertexData, f.x, t.y, t.z, u1, v1, 0f, 0f, 1f, transform, normalMat, uvModifier);
                }
            }
        }
    }

    private static void addVertices(List<Float> arr, ModuleMaterials.MeshData meshData, Vector4f uvModifier) {

        // Scale down by 1/16 and rotate around its origin:
        Vector3f o = meshData.origin();
        Vector3f r = meshData.rotation();
        Matrix4f transform = new Matrix4f()
                .scale(1.0f / 16)
                .translate(o.x, o.y, o.z)
                .rotate(new Quaternionf().rotationXYZ(r.x * MathUtils.DEG_TO_RAD, r.y * MathUtils.DEG_TO_RAD, r.z * MathUtils.DEG_TO_RAD)) // Meshes use XYZ rotation order! This is different from other part types!
                // .translate(-o.x, -o.y, -o.z) // Meshes use their origins as translations, unlike cubes which use them only as pivot points, which is why this is commented out!
        ;
        // Also scale normal matrix to compensate
        Matrix3f normalMat = transform.normal(new Matrix3f()).scale(1.0f / 16);

        // Create the faces...
        List<ModuleMaterials.VertexData> vertices = meshData.vertices();
        List<Vector2f> uvs = meshData.uvs();
        int uv = 0;
        for (Vector4i face : meshData.indices()) {
            // Always do 3 vertices
            ModuleMaterials.VertexData v1 = vertices.get(face.x);
            ModuleMaterials.VertexData v2 = vertices.get(face.y);
            ModuleMaterials.VertexData v3 = vertices.get(face.z);
            Vector3f normal = computeNormal(v1.pos(), v2.pos(), v3.pos()).mul(1.0f / 16); // Scale the normal by 1/16 as well
            meshVert(arr, v1, normal, uvs.get(uv++), transform, normalMat, uvModifier);
            meshVert(arr, v2, normal, uvs.get(uv++), transform, normalMat, uvModifier);
            meshVert(arr, v3, normal, uvs.get(uv++), transform, normalMat, uvModifier);
            if (face.w != -1) {
                // This is a quad, add the 4th vertex
                ModuleMaterials.VertexData v4 = vertices.get(face.w);
                meshVert(arr, v4, normal, uvs.get(uv++), transform, normalMat, uvModifier);
            } else {
                // This is a triangle but minecraft likes quads, so emit the 3rd vertex again
                meshVert(arr, v3, normal, uvs.get(uv - 1), transform, normalMat, uvModifier);
            }
        }
    }

    private static Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        return v2.sub(v1, new Vector3f()).cross(v3.sub(v1, new Vector3f())).normalize();
    }

    private static void meshVert(List<Float> arr, ModuleMaterials.VertexData vertexData, Vector3f normalVec, Vector2f uv, Matrix4f transform, Matrix3f normalMat, Vector4f uvModifier) {
        Vector3f p = vertexData.pos();
        if (vertexData.skinningData() == null) {
            emitVert(arr,
                    p.x, p.y, p.z, uv.x, uv.y, normalVec.x, normalVec.y, normalVec.z,
                    0, -1, -1, -1,
                    1f, 0f, 0f, 0f,
                    transform, normalMat, uvModifier
            );
        } else {
            Vector4i so = vertexData.skinningData().offsets();
            Vector4f sw = vertexData.skinningData().weights();
            emitVert(arr,
                    p.x, p.y, p.z, uv.x, uv.y, normalVec.x, normalVec.y, normalVec.z,
                    so.x, so.y, so.z, so.w, sw.x, sw.y, sw.z, sw.w,
                    transform, normalMat, uvModifier
            );
        }
    }

    private static void noSkinVert(List<Float> arr, float x, float y, float z, float u, float v, float nx, float ny, float nz, @Nullable Matrix4f transform, @Nullable Matrix3f normalMat, Vector4f uvModifier) {
        emitVert(arr, x, y, z, u, v, nx, ny, nz, 0, -1, -1, -1, 1f, 0f, 0f, 0f, transform, normalMat, uvModifier);
    }

    private static void emitVert(
            List<Float> arr,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz,
            int skinningOffset0, int skinningOffset1, int skinningOffset2, int skinningOffset3,
            float skinningWeight0, float skinningWeight1, float skinningWeight2, float skinningWeight3,
            @Nullable Matrix4f transform, @Nullable Matrix3f normalMat, Vector4f uvModifier
    ) {
        // Pos
        Vector3f pos = new Vector3f(x, y, z);
        if (transform != null) pos.mulPosition(transform);
        arr.add(pos.x); arr.add(pos.y); arr.add(pos.z);
        // Normal
        Vector3f norm = new Vector3f(nx, ny, nz);
        if (normalMat != null) norm.mul(normalMat);
        norm.normalize();
        arr.add(norm.x); arr.add(norm.y); arr.add(norm.z);
        // UV, modified by the modifier
        arr.add(u * uvModifier.z + uvModifier.x); arr.add(v * uvModifier.w + uvModifier.y);
        // Mesh skinning
        arr.add((float) skinningOffset0); arr.add((float) skinningOffset1); arr.add((float) skinningOffset2); arr.add((float) skinningOffset3);
        arr.add(skinningWeight0); arr.add(skinningWeight1); arr.add(skinningWeight2); arr.add(skinningWeight3);
    }

    public void setRenderType(@Nullable FiguraRenderType renderType) {
        this.renderType = renderType;
    }

    public @Nullable FiguraRenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public PartTransform getTransform() {
        return transform;
    }

    @Override
    public @Nullable FiguraModelPart getChildByName(String name) {
        return children.get(name);
    }


}
