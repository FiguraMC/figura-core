package org.figuramc.figura_core.text;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Provider that takes in certain inputs (accessible while rendering text) and outputs T.
 * If a style makes use of molang, make sure to update the relevant fields of the Avatar's
 * Molang component before fetching the value from this!
 */
public abstract class StyleElementProvider<T> {

    public final boolean isDynamic;
    public StyleElementProvider(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    // Get the value, given relevant parameters
    // Parameters are defined in the Molang class!
    public abstract T value(float char_index);

    public static class Constant<T> extends StyleElementProvider<T> {

        public static final Constant<Boolean> TRUE = new Constant<>(true);
        public static final Constant<Boolean> FALSE = new Constant<>(false);
        public static final Constant<Float> ZERO = new Constant<>(0f);
        public static final Constant<Float> ONE = new Constant<>(1f);
        public static final Constant<Vector2f> VEC2_ZEROES = new Constant<>(new Vector2f(0));
        public static final Constant<Vector2f> VEC2_ONES = new Constant<>(new Vector2f(1));
        public static final Constant<Vector4f> VEC4_ZEROES = new Constant<>(new Vector4f(0));
        public static final Constant<Vector4f> VEC4_ONES = new Constant<>(new Vector4f(1));


        public final T value;
        public Constant(T value) { super(false); this.value = value; }

        @Override public T value(float char_index) { return value; }
    }

    public static class Dynamic<T> extends StyleElementProvider<T> {
        private final DynamicImpl<T> impl;

        public Dynamic(DynamicImpl<T> impl) { this(impl, true); }
        public Dynamic(DynamicImpl<T> impl, boolean isDynamic) {
            super(isDynamic);
            this.impl = impl;
        }

        @Override
        public T value(float char_index) {
            return impl.value(char_index);
        }

        @FunctionalInterface public interface DynamicImpl<T> {
            T value(float char_index);
        }
    }
}
