package org.figuramc.figura_core.animation;

import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_molang.CompiledMolang;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class Vec3Keyframe implements Comparable<Vec3Keyframe> {

    // Time at which this keyframe occurs, in seconds.
    private final float time;
    // XYZ values provided by some impl
    private final AnimVec3Supplier xyz;
    // Interpolation to be used by this keyframe
    private final Interpolation interpolation;

    // Size estimate in bytes (not counting FloatSupplier implementation)
    private static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.FLOAT_SIZE
            + AllocationTracker.REFERENCE_SIZE * 4;

    // Animation name for error reporting
    public Vec3Keyframe(@Nullable String modelName, String animName, String partName, ModuleMaterials.TransformKeyframeMaterials materials, @NotNull Molang molangState, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarInitError, AvatarOutOfMemoryError {
        this.time = materials.time();
        Float x = tryParseFloat(materials.x());
        Float y = tryParseFloat(materials.y());
        Float z = tryParseFloat(materials.z());
        this.xyz = new AnimVec3Supplier.ThreeFloats( // TODO add vector mode
                x != null ? __ -> x : supplierFromMolang(modelName, animName, partName, time, molangState, materials.x()),
                y != null ? __ -> y : supplierFromMolang(modelName, animName, partName, time, molangState, materials.y()),
                z != null ? __ -> z : supplierFromMolang(modelName, animName, partName, time, molangState, materials.z())
        );
        this.interpolation = switch (materials.interpolation()) {
            case ModuleMaterials.InterpolationMaterials.Linear __ -> Interpolation.LINEAR;
            case ModuleMaterials.InterpolationMaterials.CatmullRom __ -> Interpolation.CATMULLROM;
            case ModuleMaterials.InterpolationMaterials.Step __ -> Interpolation.STEP;
            case ModuleMaterials.InterpolationMaterials.Bezier bezier -> throw new UnsupportedOperationException("Bezier interpolation is TODO");
        };
        if (allocationTracker != null)
            allocationTracker.track(this, SIZE_ESTIMATE);
    }

    // Molang expression in model %s, in animation %s, for part %s, at time %s should return 1 output, but it returns %s.
    private static final Translatable<TranslatableItems.Items5<String, String, String, Float, Integer>> INVALID_MOLANG_RETURN_COUNT
            = Translatable.create("figura_core.error.loading.animation.invalid_return_count",
            String.class, String.class, String.class, Float.class, Integer.class);
    // Molang expression in model %s, in animation %s, for part %s, at time %s failed to compile:\n%s
    private static final Translatable<TranslatableItems.Items5<String, String, String, Float, String>> INVALID_MOLANG
            = Translatable.create("figura_core.error.loading.animation.invalid_molang",
            String.class, String.class, String.class, Float.class, String.class);


    // Cringe
    private static @Nullable Float tryParseFloat(String str) {
        try { return Float.parseFloat(str); }
        catch (NumberFormatException e) { return null; }
    }
    private static AnimFloatSupplier supplierFromMolang(@Nullable String modelName, String animName, String partName, float time, Molang molang, String code) throws AvatarInitError, AvatarOutOfMemoryError {
        try {
            CompiledMolang<?> compiled = molang.compileAnimExpr(code);
            if (compiled.returnCount != 1)
                throw new AvatarInitError(INVALID_MOLANG_RETURN_COUNT, new TranslatableItems.Items5<>(modelName, animName, partName, time, compiled.returnCount));
            // ARGUMENTS: (anim_time)
            return instance -> compiled.evaluate(instance.getTime()).get(0);
        } catch (MolangCompileException compileFail) {
            throw new AvatarInitError(INVALID_MOLANG, new TranslatableItems.Items5<>(modelName, animName, partName, time, compileFail.getMessage()));
        }
    }

    // Evaluate the keyframe into the vector, also return the vector for chaining
    public Vector3f evaluateInto(Vector3f output, AnimationInstance instance) {
        xyz.fillVec3(output, instance);
        return output;
    }

    // Static cache temporary vectors. Recall the output vector may also be used as a cache vector
    private static final Vector3f p0 = new Vector3f(), p1 = new Vector3f(), p2 = new Vector3f(), p3 = new Vector3f();

    // Static helper to convert a sorted list of keyframes + a time into a vec3, also return vector for chaining
    // Should be called only on one thread, since static cache vectors are used.
    // May cause an error for the owner of the keyframes. (TODO)
    public static Vector3f evaluateTimelineInto(Vector3f output, List<Vec3Keyframe> timeline, float time, AnimationInstance instance) {
        if (timeline.isEmpty()) throw new IllegalArgumentException("Internal Figura error - attempt to interpolate animation timeline with no keyframes");

        // Binary search to find keyframe (todo: can likely improve this by searching linearly from a stored "prev keyframe"?)
        int low = 0, high = timeline.size();
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (timeline.get(mid).time < time) low = mid + 1;
            else high = mid;
        }

        // low-1 is the index of the current/prev keyframe.
        // low is the index of the next keyframe.

        // If we're not between two keyframes, snap to the closer keyframe
        if (low == 0) return timeline.getFirst().evaluateInto(output, instance);
        if (low == timeline.size()) return timeline.getLast().evaluateInto(output, instance);

        // Otherwise, perform interpolation on previous and next keyframes
        Vec3Keyframe prev = timeline.get(low - 1);
        // If interpolation is STEP, return prev directly
        if (prev.interpolation == Interpolation.STEP) return prev.evaluateInto(output, instance);
        // Otherwise, fetch next and alpha so we can interpolate!
        Vec3Keyframe next = timeline.get(low);
        float alpha = (time - prev.time) / (next.time - prev.time);

        if (prev.interpolation == Interpolation.LINEAR) {
            prev.evaluateInto(output, instance);
            next.evaluateInto(p0, instance);
            return output.lerp(p0, alpha);
        } else if (prev.interpolation == Interpolation.CATMULLROM) {
            if (low >= 2) {
                Vec3Keyframe prevPrev = timeline.get(low - 2);
                prevPrev.evaluateInto(p0, instance);
                prev.evaluateInto(p1, instance);
            } else {
                prev.evaluateInto(p0, instance);
                p1.set(p0);
            }
            next.evaluateInto(p2, instance);
            if (low + 1 < timeline.size()) {
                Vec3Keyframe nextNext = timeline.get(low + 1);
                nextNext.evaluateInto(p3, instance);
            } else {
                p3.set(p2);
            }

            // Perform catmull-rom
            // TODO benchmark this vector impl against channel-wise impl with floats and see if it's actually better
//            return output.set(
//                    catmullrom(alpha, p0.x, p1.x, p2.x, p3.x),
//                    catmullrom(alpha, p0.y, p1.y, p2.y, p3.y),
//                    catmullrom(alpha, p0.z, p1.z, p2.z, p3.z)
//            );

            p0.sub(p2).mul(-0.5f); // v0 stored in p0
            p3.sub(p1).mul(0.5f); // v1 stored in p3
            p2.sub(p1); // d stored in p2
            return output
                    .set(p2).mul(-2.0f) // - 2 * d
                    .add(p0) // + v0
                    .add(p3) // + v1
                    .mul(alpha)
                    .add(p2.mul(3.0f)) // + 3 * d
                    // p2 is now available, no longer used, so use as temporary for 2*v0
                    .sub(p0.mul(2.0f, p2)) // - 2 * v0
                    .sub(p3) // - v1
                    .mul(alpha)
                    .add(p0) // + v0
                    .mul(alpha)
                    .add(p1); // + p1
        }
        throw new IllegalArgumentException("Unexpected interpolation");
    }

    // Catmullrom on floats
    // Used as reference to create the vector catmull-rom above
    private static float catmullrom(float t, float p0, float p1, float p2, float p3) {
        float v0 = (p2 - p0) * 0.5f;
        float v1 = (p3 - p1) * 0.5f;
        float d = p2 - p1;
        return (((v0 + v1 - 2.0f * d) * t + 3.0f * d - 2.0f * v0 - v1) * t + v0) * t + p1;
        // Original impl fetched from online:
//        float t2 = t * t;
//        return (v0 + v1 - 2.0f * d) * t2 * t + (3.0f * d - 2.0f * v0 - v1) * t2 + v0 * t + p1;
    }

    public enum Interpolation {
        LINEAR,
        STEP,
        CATMULLROM,
        // TODO bezier is weird since it has additional data, so we'll get to that later :P
    }

    // Make them sortable by time in a list
    @Override
    public int compareTo(@NotNull Vec3Keyframe o) {
        return Float.compare(time, o.time);
    }

    // Interface to fill a Vec3f
    private interface AnimVec3Supplier {
        void fillVec3(Vector3f vec3f, AnimationInstance instance); // Fill the given vec3.
        // Three float values
        record ThreeFloats(AnimFloatSupplier x, AnimFloatSupplier y, AnimFloatSupplier z) implements AnimVec3Supplier {
            @Override
            public void fillVec3(Vector3f vector3f, AnimationInstance instance) {
                vector3f.set(x.get(instance), y.get(instance), z.get(instance));
            }
        }
        // Vector mode, one molang expr evaluating to a vec3
        record VectorMode(CompiledMolang<Molang> molang) implements AnimVec3Supplier {
            @Override
            public void fillVec3(Vector3f vector3f, AnimationInstance instance) {
                CompiledMolang.FloatArraySlice slice = molang.evaluate(instance.getTime());
                vector3f.set(slice.get(0), slice.get(1), slice.get(2));
            }
        }
    }

    // Interface to fetch a float
    @FunctionalInterface
    private interface AnimFloatSupplier {
        float get(AnimationInstance instance);
    }
}
