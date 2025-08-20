package org.figuramc.figura_core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura_core.util.JsonUtils;
import org.figuramc.figura_core.util.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.TreeMap;

public class AnimationImporter {

    public static ModuleMaterials.AnimationMaterials parseAnimation(JsonObject animation) {
        float length = animation.get("length").getAsFloat();
        @Nullable Float snapping = JsonUtils.getFloatOrDefault(animation, "snapping", null);
        float strength = JsonUtils.getFloatOrDefault(animation, "strength", 1.0f);
        String loopModeStr = JsonUtils.getStringOrDefault(animation, "loop", "once");
        ModuleMaterials.LoopModeMaterials loopMode = switch (loopModeStr) {
            case "hold" -> ModuleMaterials.LoopModeMaterials.HOLD;
            case "loop" -> ModuleMaterials.LoopModeMaterials.LOOP;
            default -> ModuleMaterials.LoopModeMaterials.ONCE;
        };

        TreeMap<String, ModuleMaterials.TransformKeyframesMaterials> parts = MapUtils.mapValues(
                JsonUtils.getObjectOrEmpty(animation, "parts", () -> new RuntimeException("Animation parts must be object")).asMap(),
                keyframeMatsElem -> {
                    JsonObject keyframeMats = keyframeMatsElem.getAsJsonObject();
                    return new ModuleMaterials.TransformKeyframesMaterials(
                            JsonUtils.getListOrEmpty(keyframeMats, "origin", k -> parseVectorKeyframe(k.getAsJsonObject(), snapping), () -> new RuntimeException("Animation origin must be array")),
                            JsonUtils.getListOrEmpty(keyframeMats, "rotation", k -> parseVectorKeyframe(k.getAsJsonObject(), snapping), () -> new RuntimeException("Animation rotation must be array")),
                            JsonUtils.getListOrEmpty(keyframeMats, "scale", k -> parseVectorKeyframe(k.getAsJsonObject(), snapping), () -> new RuntimeException("Animation scale must be array"))
                    );
                },
                TreeMap::new
        );

        return new ModuleMaterials.AnimationMaterials(length, snapping, strength, loopMode, parts, List.of()); // TODO script keyframes

    }

    private static ModuleMaterials.TransformKeyframeMaterials parseVectorKeyframe(JsonObject keyframe, @Nullable Float snapping) {
        float time = keyframe.get("time").getAsFloat();
        if (snapping != null) time /= snapping;
        JsonArray arr = keyframe.getAsJsonArray("data");
        String x = arr.get(0).getAsJsonPrimitive().getAsString();
        String y = arr.get(1).getAsJsonPrimitive().getAsString();
        String z = arr.get(2).getAsJsonPrimitive().getAsString();
        ModuleMaterials.InterpolationMaterials interpolation = switch (keyframe.get("interpolation")) {
            case JsonPrimitive p -> switch (p.getAsString()) {
                case "linear" -> ModuleMaterials.InterpolationMaterials.Linear.INSTANCE;
                case "catmullrom" -> ModuleMaterials.InterpolationMaterials.CatmullRom.INSTANCE;
                case "step" -> ModuleMaterials.InterpolationMaterials.Step.INSTANCE;
                default -> throw new RuntimeException("Unrecognized interpolation \"" + p.getAsString() + "\"");
            };
            case JsonObject obj -> switch (JsonUtils.getStringOrDefault(obj, "kind", "null")) {
                case "bezier" -> new ModuleMaterials.InterpolationMaterials.Bezier(
                        JsonUtils.parseVec3f(obj.getAsJsonArray("left_time"), () -> new Vector3f(-0.1f)),
                        JsonUtils.parseVec3f(obj.getAsJsonArray("left_value"), Vector3f::new),
                        JsonUtils.parseVec3f(obj.getAsJsonArray("right_time"), () -> new Vector3f(0.1f)),
                        JsonUtils.parseVec3f(obj.getAsJsonArray("right_value"), Vector3f::new)
                );
                default -> throw new RuntimeException("Unrecognized interpolation kind \"" + JsonUtils.getStringOrDefault(obj, "kind", "null"));
            };
            default -> throw new RuntimeException("Unrecognized interpolation");
        };
        return new ModuleMaterials.TransformKeyframeMaterials(time, x, y, z, interpolation);
    }

}
