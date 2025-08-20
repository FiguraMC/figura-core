package org.figuramc.figura_core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura_core.util.functional.ThrowingFunction;
import org.figuramc.figura_core.util.functional.ThrowingSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Supplier;

public class JsonUtils {

    // Json -> Java

    @SuppressWarnings("SameParameterValue")
    @Contract("_, _, !null -> !null")
    public static @Nullable String getStringOrDefault(JsonObject object, String key, @Nullable String defaultVal) {
        JsonElement elem = object.get(key);
        if (elem == null || !elem.isJsonPrimitive()) return defaultVal;
        JsonPrimitive prim = elem.getAsJsonPrimitive();
        if (!prim.isString()) return defaultVal;
        return prim.getAsString();
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable JsonElement getElementOrDefault(JsonObject object, String key, @Nullable JsonElement defaultVal) {
        JsonElement elem = object.get(key);
        return elem == null ? defaultVal : elem;
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable Integer getIntOrDefault(JsonObject object, String key, @Nullable Integer defaultVal) {
        JsonElement elem = object.get(key);
        if (elem == null || !elem.isJsonPrimitive()) return defaultVal;
        JsonPrimitive prim = elem.getAsJsonPrimitive();
        if (!prim.isNumber()) return defaultVal;
        return prim.getAsInt();
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable Float getFloatOrDefault(JsonObject object, String key, @Nullable Float defaultVal) {
        JsonElement elem = object.get(key);
        if (elem == null || !elem.isJsonPrimitive()) return defaultVal;
        JsonPrimitive prim = elem.getAsJsonPrimitive();
        if (!prim.isNumber()) return defaultVal;
        return prim.getAsFloat();
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable Boolean getBooleanOrDefault(JsonObject object, String key, @Nullable Boolean defaultVal) {
        JsonElement elem = object.get(key);
        if (elem == null || !elem.isJsonPrimitive()) return defaultVal;
        JsonPrimitive prim = elem.getAsJsonPrimitive();
        if (!prim.isBoolean()) return defaultVal;
        return prim.getAsBoolean();
    }

    public static <T, E extends Throwable> List<T> getListOrEmpty(JsonObject object, String key, ThrowingFunction<JsonElement, T, E> tFetcher, Supplier<E> ifNotArray) throws E {
        JsonElement elem = object.get(key);
        if (elem == null) return List.of();
        if (!elem.isJsonArray()) throw ifNotArray.get();
        return ListUtils.map(elem.getAsJsonArray(), tFetcher);
    }

    public static <E extends Throwable> JsonObject getObjectOrEmpty(JsonObject object, String key, Supplier<E> ifNotObject) throws E {
        JsonElement elem = object.get(key);
        if (elem == null) return new JsonObject();
        if (!elem.isJsonObject()) throw ifNotObject.get();
        return elem.getAsJsonObject();
    }

    public static <E extends Throwable> Vector2f parseVec2f(@Nullable JsonArray arr, ThrowingSupplier<Vector2f, E> defaultValue) throws E {
        if (arr == null) return defaultValue.get();
        if (arr.size() != 2) throw new IllegalArgumentException("Vector is not length 2");
        Vector2f res = new Vector2f();
        for (int i = 0; i < 2; i++)
            res.setComponent(i, arr.get(i).getAsFloat());
        return res;
    }

    public static <E extends Throwable> Vector3f parseVec3f(@Nullable JsonArray arr, ThrowingSupplier<Vector3f, E> defaultValue) throws E {
        if (arr == null) return defaultValue.get();
        if (arr.size() != 3) throw new IllegalArgumentException("Vector is not length 3");
        Vector3f res = new Vector3f();
        for (int i = 0; i < 3; i++)
            res.setComponent(i, arr.get(i).getAsFloat());
        return res;
    }

    // Java -> Json
    public static JsonArray toJson(Vector3fc vec) {
        return toJson(vec.x(), vec.y(), vec.z());
    }

    public static JsonArray toJson(Vector2fc vec) {
        return toJson(vec.x(), vec.y());
    }

    public static final JsonArray ZERO_VEC_3 = toJson(0, 0, 0);

    public static JsonArray toJson(float... values) {
        JsonArray arr = new JsonArray();
        for (float f : values) arr.add(f);
        return arr;
    }

}
