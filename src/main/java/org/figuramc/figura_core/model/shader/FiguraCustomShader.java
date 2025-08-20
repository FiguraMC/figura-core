package org.figuramc.figura_core.model.shader;

import java.util.List;
import java.util.Map;

/**
 * A custom shader does not write raw GLSL.
 * Instead, it stores what it does in a data structure.
 * This should help make it portable across rendering backends.
 */
public record FiguraCustomShader(
        List<VertexElem> customVertexElements,
        List<Uniform> customUniforms,
        List<Buffer> customBuffers
) {

    public record VertexElem(String name, VariableType type) {}
    public record Uniform(String name, VariableType type) {}
    public record Buffer(String name, VariableType type) {}

    // Types of variables
    public interface VariableType {
        String name();

        VariableType FLOAT = () -> "float";
        VariableType VEC2 = () -> "vec2";
        VariableType VEC3 = () -> "vec3";
        VariableType VEC4 = () -> "vec4";

        VariableType INT = () -> "int";
        VariableType IVEC2 = () -> "ivec2";
        VariableType IVEC3 = () -> "ivec3";
        VariableType IVEC4 = () -> "ivec4";

        VariableType MAT2 = () -> "mat2";
        VariableType MAT3 = () -> "mat3";
        VariableType MAT4 = () -> "mat4";

        record CustomStruct(String name, Map<String, VariableType> fields) implements VariableType {}
    }

}
