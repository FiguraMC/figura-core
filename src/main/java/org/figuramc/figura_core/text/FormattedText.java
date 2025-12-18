package org.figuramc.figura_core.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.util.JsonUtils;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a string of text with some formatting applied to it.
 */
public class FormattedText {

    // Text, stored in utf-32
    public final int[] codepoints;
    // Style
    public final TextStyle style;
    // Children of this text, which come after it
    public final List<FormattedText> children;

    /**
     * Construct a basic piece of text from a string with no formatting.
     */
    public FormattedText(String text) {
        codepoints = text.codePoints().toArray();
        style = TextStyle.NO_STYLE;
        children = List.of();
    }

    /**
     * Verbose constructor with all fields
     */
    public FormattedText(int[] codepoints, TextStyle style, List<FormattedText> children) {
        this.codepoints = codepoints;
        this.style = style;
        this.children = children;
    }

    /**
     * Parse text from JSON
     */
    public FormattedText(JsonElement element, TextStyle parentStyle, @Nullable Molang molang) throws AvatarError, MolangCompileException { this(element, parentStyle, molang, new int[1]); }
    private FormattedText(JsonElement element, TextStyle parentStyle, @Nullable Molang molang, int[] curCharIndex) throws AvatarError, MolangCompileException {
        switch (element) {
            case JsonPrimitive p -> {
                codepoints = p.getAsString().codePoints().toArray();
                curCharIndex[0] += codepoints.length;
                style = parentStyle;
                children = List.of();
            }
            case JsonArray array -> {
                codepoints = new int[0];
                style = parentStyle;
                children = ListUtils.<JsonElement, FormattedText, AvatarError, MolangCompileException>mapBiThrowing(array, child -> new FormattedText(child, style, molang, curCharIndex));
            }
            case JsonObject object -> {
                codepoints = JsonUtils.getStringOrDefault(object, "text", "").codePoints().toArray();
                int charCount = calculateCharCount(object);
                style = TextStyle.parseJson(parentStyle, object, molang, curCharIndex[0], charCount);
                curCharIndex[0] += codepoints.length;
                if (object.has("extra")) {
                    children = new ArrayList<>();
                    for (JsonElement extra : object.getAsJsonArray("extra"))
                        children.add(new FormattedText(extra, style, molang, curCharIndex));
                } else children = List.of();
            }
            default -> throw new IllegalArgumentException("Json must be primitive, array, or object");
        }
    }

    // This is inefficient, but we need the total char count in order to parse the TextStyle,
    // and we need the parent TextStyle to create the child TextStyle...
    // This whole situation is cursed, honestly.
    private int calculateCharCount(JsonElement elem) {
        return switch (elem) {
            case JsonPrimitive p -> {
                String s = p.getAsString();
                yield s.codePointCount(0, s.length());
            }
            case JsonArray array -> {
                int count = 0;
                for (var item : array) count += calculateCharCount(item);
                yield count;
            }
            case JsonObject object -> {
                int count = 0;
                String text = JsonUtils.getStringOrDefault(object, "text", null);
                if (text != null) count = text.codePointCount(0, text.length());
                if (object.has("extra"))
                    count += calculateCharCount(object.get("extra"));
                yield count;
            }
            default -> throw new IllegalArgumentException("Json must be primitive, array, or object");
        };
    }

    // -1 = uncalculated, 0 = calculated as false, 1 = calculated as true
    private byte isDynamicCached = -1;
    public boolean isDynamic() {
        if (isDynamicCached == -1) {
            isDynamicCached = 0;
            if (style.isDynamic) isDynamicCached = 1;
            for (FormattedText child : children) {
                if (child.isDynamic()) isDynamicCached = 1;
            }
        }
        return isDynamicCached != 0;
    }
}