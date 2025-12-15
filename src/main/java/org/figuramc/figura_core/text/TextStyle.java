package org.figuramc.figura_core.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextStyle {

    public static final TextStyle NO_STYLE = new TextStyle();

    public final StyleElementProvider<Boolean> bold, italic, obfuscated;
    public final StyleElementProvider<Float> verticalAlignment;
    public final StyleElementProvider<Vector2f> scale, outlineScale, skew, offset, shadowOffset;
    public final StyleElementProvider<Vector4f> color, backgroundColor, shadowColor, strikethroughColor, underlineColor, outlineColor;

    private TextStyle() {
        bold = italic = obfuscated = StyleElementProvider.Constant.FALSE;
        verticalAlignment = StyleElementProvider.Constant.ZERO;
        scale = outlineScale = shadowOffset = StyleElementProvider.Constant.VEC2_ONES;
        skew = offset = StyleElementProvider.Constant.VEC2_ZEROES;
        color = StyleElementProvider.Constant.VEC4_ONES;
        backgroundColor = shadowColor = strikethroughColor = underlineColor = outlineColor = StyleElementProvider.Constant.VEC4_ZEROES;
    }

    public TextStyle(
            @NotNull TextStyle parent,
            @Nullable StyleElementProvider<Boolean> bold,
            @Nullable StyleElementProvider<Boolean> italic,
            @Nullable StyleElementProvider<Boolean> obfuscated,
            @Nullable StyleElementProvider<Float> verticalAlignment,
            @Nullable StyleElementProvider<Vector2f> scale,
            @Nullable StyleElementProvider<Vector2f> outlineScale,
            @Nullable StyleElementProvider<Vector2f> skew,
            @Nullable StyleElementProvider<Vector2f> offset,
            @Nullable StyleElementProvider<Vector2f> shadowOffset,
            @Nullable StyleElementProvider<Vector4f> color,
            @Nullable StyleElementProvider<Vector4f> backgroundColor,
            @Nullable StyleElementProvider<Vector4f> shadowColor,
            @Nullable StyleElementProvider<Vector4f> strikethroughColor,
            @Nullable StyleElementProvider<Vector4f> underlineColor,
            @Nullable StyleElementProvider<Vector4f> outlineColor
    ) {
        this.bold = bold != null ? bold : parent.bold;
        this.italic = italic != null ? italic : parent.italic;
        this.obfuscated = obfuscated != null ? obfuscated : parent.obfuscated;
        this.verticalAlignment = verticalAlignment != null ? verticalAlignment : parent.verticalAlignment;
        this.scale = scale != null ? scale : parent.scale;
        this.outlineScale = outlineScale != null ? outlineScale : parent.outlineScale;
        this.skew = skew != null ? skew : parent.skew;
        this.offset = offset != null ? offset : parent.offset;
        this.shadowOffset = shadowOffset != null ? shadowOffset : parent.shadowOffset;
        this.color = color != null ? color : parent.color;
        this.backgroundColor = backgroundColor != null ? backgroundColor : parent.backgroundColor;
        this.shadowColor = shadowColor != null ? shadowColor : parent.shadowColor;
        this.strikethroughColor = strikethroughColor != null ? strikethroughColor : parent.strikethroughColor;
        this.underlineColor = underlineColor != null ? underlineColor : parent.underlineColor;
        this.outlineColor = outlineColor != null ? outlineColor : parent.outlineColor;
    }

    public static class Builder {
        private @Nullable StyleElementProvider<Boolean> bold, italic, obfuscated;
        private @Nullable StyleElementProvider<Float> verticalAlignment;
        private @Nullable StyleElementProvider<Vector2f> scale, outlineScale, skew, offset, shadowOffset;
        private @Nullable StyleElementProvider<Vector4f> color, backgroundColor, shadowColor, strikethroughColor, underlineColor, outlineColor;

        public TextStyle build() {
            return new TextStyle(NO_STYLE, bold, italic, obfuscated, verticalAlignment, scale, outlineScale, skew, offset, shadowOffset, color, backgroundColor, shadowColor, strikethroughColor, underlineColor, outlineColor);
        }

        public Builder bold(StyleElementProvider<Boolean> bold) { this.bold = bold; return this; }
        public Builder italic(StyleElementProvider<Boolean> italic) { this.italic = italic; return this; }
        public Builder obfuscated(StyleElementProvider<Boolean> obfuscated) { this.obfuscated = obfuscated; return this; }
        public Builder verticalAlignment(StyleElementProvider<Float> verticalAlignment) { this.verticalAlignment = verticalAlignment; return this; }
        public Builder scale(StyleElementProvider<Vector2f> scale) { this.scale = scale; return this; }
        public Builder outlineScale(StyleElementProvider<Vector2f> outlineScale) { this.outlineScale = outlineScale; return this; }
        public Builder skew(StyleElementProvider<Vector2f> skew) { this.skew = skew; return this; }
        public Builder offset(StyleElementProvider<Vector2f> offset) { this.offset = offset; return this; }
        public Builder shadowOffset(StyleElementProvider<Vector2f> shadowOffset) { this.shadowOffset = shadowOffset; return this; }
        public Builder color(StyleElementProvider<Vector4f> color) { this.color = color; return this; }
        public Builder backgroundColor(StyleElementProvider<Vector4f> backgroundColor) { this.backgroundColor = backgroundColor; return this; }
        public Builder shadowColor(StyleElementProvider<Vector4f> shadowColor) { this.shadowColor = shadowColor; return this; }
        public Builder strikethroughColor(StyleElementProvider<Vector4f> strikethroughColor) { this.strikethroughColor = strikethroughColor; return this; }
        public Builder underlineColor(StyleElementProvider<Vector4f> underlineColor) { this.underlineColor = underlineColor; return this; }
        public Builder outlineColor(StyleElementProvider<Vector4f> outlineColor) { this.outlineColor = outlineColor; return this; }
    }

    public static TextStyle parseJson(TextStyle parent, JsonObject object, @Nullable Molang molang, int startCharIndex, int charCount) throws AvatarError, MolangCompileException {
        return new TextStyle(
                parent,
                parseBoolean(object.get("bold"), molang, startCharIndex, charCount),
                parseBoolean(object.get("italic"), molang, startCharIndex, charCount),
                parseBoolean(object.get("obfuscated"), molang, startCharIndex, charCount),
                parseFloat(object.get("verticalAlignment"), molang, startCharIndex, charCount),
                parseVec2(object.get("scale"), molang, startCharIndex, charCount),
                parseVec2(object.get("outlineScale"), molang, startCharIndex, charCount),
                parseVec2(object.get("skew"), molang, startCharIndex, charCount),
                parseVec2(object.get("offset"), molang, startCharIndex, charCount),
                parseVec2(object.get("shadowOffset"), molang, startCharIndex, charCount),
                parseVec4(object.get("color"), molang, startCharIndex, charCount),
                parseVec4(object.get("backgroundColor"), molang, startCharIndex, charCount),
                parseVec4(object.get("shadowColor"), molang, startCharIndex, charCount),
                parseVec4(object.get("strikethroughColor"), molang, startCharIndex, charCount),
                parseVec4(object.get("underlineColor"), molang, startCharIndex, charCount),
                parseVec4(object.get("outlineColor"), molang, startCharIndex, charCount)
        );
    }

    // TODO better errors for invalid inputs

    @Contract("!null, _, _, _ -> !null")
    private static @Nullable StyleElementProvider<Boolean> parseBoolean(@Nullable JsonElement e, @Nullable Molang molang, int startCharIndex, int charCount) throws AvatarError, MolangCompileException {
        if (e == null) return null;
        if (!(e instanceof JsonPrimitive p)) throw new IllegalArgumentException("Should be primitive");
        if (p.isBoolean()) return p.getAsBoolean() ? StyleElementProvider.Constant.TRUE : StyleElementProvider.Constant.FALSE;
        if (p.isString()) {
            if (molang != null) {
                var compiled = molang.compileTextExpr(p.getAsString(), startCharIndex, charCount);
                return new StyleElementProvider.Dynamic<>(char_index -> compiled.evaluate(char_index).get(0) != 0);
            }
            throw new IllegalArgumentException("No molang instance");
        }
        throw new IllegalArgumentException("Should be bool or string");
    }

    @Contract("!null, _, _, _ -> !null")
    private static @Nullable StyleElementProvider<Float> parseFloat(@Nullable JsonElement e, @Nullable Molang molang, int startCharIndex, int charCount) throws AvatarError, MolangCompileException {
        if (e == null) return null;
        if (!(e instanceof JsonPrimitive p)) throw new IllegalArgumentException("Should be primitive");
        if (p.isNumber()) return new StyleElementProvider.Constant<>(p.getAsFloat());
        if (p.isString()) {
            if (molang != null) {
                var compiled = molang.compileTextExpr(p.getAsString(), startCharIndex, charCount);
                return new StyleElementProvider.Dynamic<>(char_index -> compiled.evaluate(char_index).get(0));
            }
            throw new IllegalArgumentException("No molang instance");
        }
        throw new IllegalArgumentException("Should be number or string");
    }

    @Contract("!null, _, _, _ -> !null")
    private static @Nullable StyleElementProvider<Vector2f> parseVec2(@Nullable JsonElement e, @Nullable Molang molang, int startCharIndex, int charCount) throws AvatarError, MolangCompileException {
        if (e == null) return null;
        // Accept array of 2 float elements
        if (e instanceof JsonArray arr) {
            if (arr.size() != 2) throw new IllegalArgumentException("Array should have 2 elements");
            StyleElementProvider<Float> x = parseFloat(arr.get(0), molang, startCharIndex, charCount);
            StyleElementProvider<Float> y = parseFloat(arr.get(1), molang, startCharIndex, charCount);
            if (x instanceof StyleElementProvider.Constant<Float> constX && y instanceof StyleElementProvider.Constant<Float> constY) {
                return new StyleElementProvider.Constant<>(new Vector2f(constX.value, constY.value));
            } else {
                Vector2f vec = new Vector2f();
                return new StyleElementProvider.Dynamic<>(char_index -> vec.set(x.value(char_index), y.value(char_index)));
            }
        }
        if (!(e instanceof JsonPrimitive p)) throw new IllegalArgumentException("Should be primitive or array");
        if (p.isNumber()) return new StyleElementProvider.Constant<>(new Vector2f(p.getAsFloat()));
        if (p.isString()) {
            if (molang != null) {
                var compiled = molang.compileTextExpr(p.getAsString(), startCharIndex, charCount);
                Vector2f vec = new Vector2f();
                if (compiled.returnCount == 1) {
                    return new StyleElementProvider.Dynamic<>(char_index -> vec.set(compiled.evaluate(char_index).get(0)));
                } else if (compiled.returnCount == 2) {
                    return new StyleElementProvider.Dynamic<>(char_index -> {
                        var out = compiled.evaluate(char_index);
                        return vec.set(out.get(0), out.get(1));
                    });
                } else throw new IllegalArgumentException("Molang must compile to 1 or 2 values");
            }
            throw new IllegalArgumentException("No molang instance");
        }
        throw new IllegalArgumentException("Should be number or string");
    }

    @Contract("!null, _, _, _ -> !null")
    private static @Nullable StyleElementProvider<Vector4f> parseVec4(@Nullable JsonElement e, @Nullable Molang molang, int startCharIndex, int charCount) throws AvatarError, MolangCompileException {
        if (e == null) return null;
        // Accept array of 4 float elements
        if (e instanceof JsonArray arr) {
            if (arr.size() != 4) throw new IllegalArgumentException("Array should have 4 elements");
            StyleElementProvider<Float> x = parseFloat(arr.get(0), molang, startCharIndex, charCount);
            StyleElementProvider<Float> y = parseFloat(arr.get(1), molang, startCharIndex, charCount);
            StyleElementProvider<Float> z = parseFloat(arr.get(2), molang, startCharIndex, charCount);
            StyleElementProvider<Float> w = parseFloat(arr.get(3), molang, startCharIndex, charCount);
            if (
                    x instanceof StyleElementProvider.Constant<Float> constX && y instanceof StyleElementProvider.Constant<Float> constY &&
                    z instanceof StyleElementProvider.Constant<Float> constZ && w instanceof StyleElementProvider.Constant<Float> constW
            ) {
                return new StyleElementProvider.Constant<>(new Vector4f(constX.value, constY.value, constZ.value, constW.value));
            } else {
                Vector4f vec = new Vector4f();
                return new StyleElementProvider.Dynamic<>(char_index -> vec.set(x.value(char_index), y.value(char_index), z.value(char_index), w.value(char_index)));
            }
        }
        if (!(e instanceof JsonPrimitive p)) throw new IllegalArgumentException("Should be primitive or array");
        if (p.isNumber()) return new StyleElementProvider.Constant<>(new Vector4f(p.getAsFloat()));
        if (p.isString()) {
            if (molang != null) {
                var compiled = molang.compileTextExpr(p.getAsString(), startCharIndex, charCount);
                Vector4f vec = new Vector4f();
                if (compiled.returnCount == 1) {
                    return new StyleElementProvider.Dynamic<>(char_index -> vec.set(compiled.evaluate(char_index).get(0)));
                } else if (compiled.returnCount == 4) {
                    return new StyleElementProvider.Dynamic<>(char_index -> {
                        var out = compiled.evaluate(char_index);
                        return vec.set(out.get(0), out.get(1), out.get(2), out.get(3));
                    });
                } else throw new IllegalArgumentException("Molang must compile to 1 or 4 values");
            }
            throw new IllegalArgumentException("No molang instance");
        }
        throw new IllegalArgumentException("Should be number or string");
    }




}
