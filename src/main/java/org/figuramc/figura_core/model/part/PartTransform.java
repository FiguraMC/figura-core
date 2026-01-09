package org.figuramc.figura_core.model.part;

import org.figuramc.figura_core.animation.Animator;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

// Simple transform, more aligned to Minecraft/Blockbench
public class PartTransform {

    private final Vector3f origin = new Vector3f();
    private final Vector3f rotation = new Vector3f(); // ZYX euler angles in radians
    private final Vector3f scale = new Vector3f(1.0f);
    private final Vector3f position = new Vector3f();

    private boolean visible = true;
    private final Vector4f color = new Vector4f(1.0f);

    private @Nullable VanillaRendering.ScriptVanillaPart mimicPart; // Mimic this vanilla part!
    private @Nullable List<Animator> animators = null;

    // Outputs and caching flags
    private final Vector3f totalOrigin = new Vector3f();
    private final Vector3f totalRotation = new Vector3f();
    private final Vector3f totalScale = new Vector3f(1.0f);
    private final Vector3f totalPosition = new Vector3f();
    private final Matrix4f totalMatrix = new Matrix4f();
    private final Matrix3f totalNormalMatrix = new Matrix3f();

    // Usually, when marking any of origin/rotation/scale as dirty, we also mark the matrix as dirty.
    // However, if the matrix is manually forced, we do not mark the matrix as dirty.
    // In that case, changes made to the origin/rotation/scale are no longer considered.
    private byte flags = 0;
    public static final byte ORIGIN_DIRTY = 0x01; // totalOrigin is dirty and needs recalculation
    public static final byte ROTATION_DIRTY = 0x02; // totalRotation is dirty and needs recalculation
    public static final byte SCALE_DIRTY = 0x04; // totalScale is dirty and needs recalculation
    public static final byte POSITION_DIRTY = 0x08; // totalScale is dirty and needs recalculation
    public static final byte MATRIX_DIRTY = 0x10; // matrix is dirty and needs recalculation

    // Size estimate
    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 13
            + AllocationTracker.VEC3F_SIZE * 8
            + AllocationTracker.VEC4F_SIZE
            + AllocationTracker.MAT3F_SIZE
            + AllocationTracker.MAT4F_SIZE;
    // Alloc state, for any future updates
    private final @Nullable AllocationTracker.State<AvatarOutOfMemoryError> allocState;
    public PartTransform(@Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        if (allocationTracker != null) {
            this.allocState = allocationTracker.track(this, SIZE_ESTIMATE);
        } else this.allocState = null;
    }

    // Copy constructor.
    public PartTransform(PartTransform transform, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
        this.origin.set(transform.origin);
        this.rotation.set(transform.rotation);
        this.scale.set(transform.scale);
        this.visible = transform.visible;
        this.color.set(transform.color);
        this.mimicPart = transform.mimicPart;
        this.animators = transform.animators == null ? null : new ArrayList<>(transform.animators);
        this.totalOrigin.set(transform.totalOrigin);
        this.totalRotation.set(transform.totalRotation);
        this.totalScale.set(transform.totalScale);
        this.totalPosition.set(transform.totalPosition);
        this.totalMatrix.set(transform.totalMatrix);
        this.totalNormalMatrix.set(transform.totalNormalMatrix);
        this.flags = transform.flags;
        // Track allocation estimate
        if (allocationTracker != null) {
            int size = SIZE_ESTIMATE;
            if (animators != null) size += AllocationTracker.REFERENCE_SIZE * animators.size();
            this.allocState = allocationTracker.track(this, size);
        } else this.allocState = null;
    }



    // Flag get/set
    public boolean hasFlags(int flags) { return (this.flags & flags) == flags; }
    public void setFlags(int flags) { this.flags |= (byte) flags; }
    public void removeFlags(int flags) { this.flags &= (byte) ~flags; }
    private boolean originDirty() { return mimicPart != null || hasFlags(ORIGIN_DIRTY); }
    private boolean rotationDirty() { return mimicPart != null || hasFlags(ROTATION_DIRTY); }
    private boolean scaleDirty() { return mimicPart != null || hasFlags(SCALE_DIRTY); }
    private boolean positionDirty() { return mimicPart != null || hasFlags(POSITION_DIRTY); }
    private boolean matrixDirty() { return mimicPart != null || hasFlags(MATRIX_DIRTY); }

    // Property set/get
    public void setOrigin(Vector3fc origin) { this.origin.set(origin); setFlags(ORIGIN_DIRTY | MATRIX_DIRTY); }
    public void setOrigin(float x, float y, float z) { this.origin.set(x, y, z); setFlags(ORIGIN_DIRTY | MATRIX_DIRTY); }
    public Vector3fc getOrigin() { return this.origin; }

    public void setEulerRad(Vector3fc rotation) { this.rotation.set(rotation); setFlags(ROTATION_DIRTY | MATRIX_DIRTY); }
    public void setEulerRad(float x, float y, float z) { this.rotation.set(x, y, z); setFlags(ROTATION_DIRTY | MATRIX_DIRTY); }
    public void setEulerDeg(Vector3fc rotation) { this.rotation.set(rotation).mul((float) (Math.PI / 180)); setFlags(ROTATION_DIRTY | MATRIX_DIRTY); }
    public void setEulerDeg(float x, float y, float z) { this.rotation.set(x, y, z).mul((float) (Math.PI / 180)); setFlags(ROTATION_DIRTY | MATRIX_DIRTY); }
    public Vector3fc getEulerRad() { return this.rotation; }

    public void setScale(Vector3fc scale) { this.scale.set(scale); setFlags(SCALE_DIRTY | MATRIX_DIRTY); }
    public void setScale(float x, float y, float z) { this.scale.set(x, y, z); setFlags(SCALE_DIRTY | MATRIX_DIRTY); }
    public void setScale(float s) { this.scale.set(s); setFlags(SCALE_DIRTY | MATRIX_DIRTY); }
    public Vector3fc getScale() { return this.scale; }

    public void setPosition(Vector3fc position) { this.position.set(position); setFlags(POSITION_DIRTY | MATRIX_DIRTY); }
    public void setPosition(float x, float y, float z) { this.position.set(x, y, z); setFlags(POSITION_DIRTY | MATRIX_DIRTY); }
    public Vector3fc getPosition() { return this.position; }

    public void forceMatrix(Matrix4fc matrix) { this.totalMatrix.set(matrix); this.totalMatrix.normal(totalNormalMatrix); removeFlags(MATRIX_DIRTY); }
    public void unforceMatrix() { setFlags(MATRIX_DIRTY); }

    public void setColor(Vector4fc color) { this.color.set(color); }
    public void setColor(float r, float g, float b, float a) { this.color.set(r, g, b, a); }

    public void setVisible(boolean vis) { this.visible = vis; }

    public void setMimicPart(@Nullable VanillaRendering.ScriptVanillaPart mimicPart) { this.mimicPart = mimicPart; }

    public void addAnimator(Animator animator) throws AvatarOutOfMemoryError {
        if (animators == null)
            animators = new ArrayList<>(1);
        animators.add(animator);
        if (allocState != null) allocState.changeSize(AllocationTracker.REFERENCE_SIZE);
        animator.addTransform(this);
    }

    public Vector3fc totalOrigin() {
        if (originDirty()) {
            totalOrigin.set(origin);
            if (mimicPart != null)
                totalOrigin.add(mimicPart.storedVanillaOrigin);
            if (animators != null)
                for (Animator animator : animators)
                    if (animator.hasOrigin())
                        totalOrigin.add(animator.getOrigin());
            removeFlags(ORIGIN_DIRTY);
        }
        return totalOrigin;
    }
    public Vector3fc totalEulerRad() {
        if (rotationDirty()) {
            totalRotation.set(rotation);
            if (mimicPart != null)
                totalRotation.add(mimicPart.storedVanillaRotation);
            if (animators != null)
                for (Animator animator : animators)
                    if (animator.hasRotation())
                        totalRotation.add(animator.getEulerRad());
            removeFlags(ROTATION_DIRTY);
        }
        return totalRotation;
    }
    public Vector3fc totalScale() {
        if (scaleDirty()) {
            totalScale.set(scale);
            if (mimicPart != null)
                totalScale.mul(mimicPart.storedVanillaScale);
            if (animators != null)
                for (Animator animator : animators)
                    if (animator.hasScale())
                        totalScale.mul(animator.getScale());
            removeFlags(SCALE_DIRTY);
        }
        return totalScale;
    }
    public Vector3fc totalPosition() {
        if (positionDirty()) {
            totalPosition.set(position);
            if (mimicPart != null) totalPosition.add(mimicPart.storedVanillaPosition);
            removeFlags(POSITION_DIRTY);
        }
        return totalPosition;
    }
    public Vector4fc getColor() {
        return color;
    }
    public boolean getVisible() {
        return visible;
    }

    // Affect the transform stack with this
    public void affect(FiguraTransformStack stack) {
        if (matrixDirty()) {
            Vector3fc origin = totalOrigin();
            Vector3fc rotation = totalEulerRad();
            Vector3fc scale = totalScale();
            Vector3fc position = totalPosition();
            totalMatrix
                    .translation(origin)
                    .rotateZYX(rotation.z(), rotation.y(), rotation.x())
                    .scale(scale)
                    .translate(position);
            totalMatrix.normal(totalNormalMatrix);
            removeFlags(MATRIX_DIRTY);
        }
        stack.multiply(totalMatrix, totalNormalMatrix);
        stack.color(color);
    }

}
