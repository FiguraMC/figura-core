package org.figuramc.figura_core.model.rendering.shader;

import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Hook points for shaders. A hook can be implemented in an ExtensionShader using GLSL.
 */
public class ShaderHookPoint extends EnumLike {

    // All vertex hooks:
    public static final ShaderHookPoint PART_SPACE_HOOK = new ShaderHookPoint(Stage.VERTEX, "figura_part_space_hook",
            List.of("inout vec3", "inout vec3", "inout vec3"), "void", null);
    public static final ShaderHookPoint MODEL_SPACE_HOOK = new ShaderHookPoint(Stage.VERTEX, "figura_model_space_hook",
            List.of("inout vec4", "inout vec3", "inout vec3", "inout vec4", "inout vec2"), "void", null);

    // All fragment hooks:

    public final Stage stage;
    public final String name;
    public final List<String> paramTypes;
    public final String returnType;
    public final String defaultImpl; // Default GLSL implementing the hook

    public ShaderHookPoint(Stage stage, String name, List<String> paramTypes, String returnType, @Nullable String defaultReturnValue) {
        this.stage = stage;
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        // Create the default impl for the hook point
        StringBuilder defaultImplBuilder = new StringBuilder();
        defaultImplBuilder.append(returnType).append(" ").append(name).append("(");
        for (int i = 0; i < paramTypes.size(); i++) {
            defaultImplBuilder.append(paramTypes.get(i));
            defaultImplBuilder.append(' ').append((char) ('a' + i));
            if (i != paramTypes.size() - 1) defaultImplBuilder.append(", ");
        }
        defaultImplBuilder.append(") {");
        if (returnType.equals("void")) {
            defaultImplBuilder.append("}");
        } else {
            defaultImplBuilder.append(" return ").append(defaultReturnValue).append("; }");
        }
        this.defaultImpl = defaultImplBuilder.toString();
    }

    public enum Stage {
        VERTEX,
        FRAGMENT
    }

}
