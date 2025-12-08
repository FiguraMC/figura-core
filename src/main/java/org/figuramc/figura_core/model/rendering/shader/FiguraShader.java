package org.figuramc.figura_core.model.rendering.shader;


import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;

import java.util.List;

public sealed interface FiguraShader permits BuiltinShader, ExtensionShader {

    FiguraVertexFormat vertexFormat();
    List<String> textureBindingPoints();


    // Extra types

    sealed interface Uniform {
        String name();

        // A singular uniform of the given kind (TODO add uniform blocks as an option? Is that even worth it?)
        record Single(String name, Type type) implements Uniform {}

        sealed interface Type {

            Type MAT2 = new Array(Raw.FLOAT2, 2);
            Type MAT3 = new Array(Raw.FLOAT3, 3);
            Type MAT4 = new Array(Raw.FLOAT4, 4);

            record Struct(List<Type> fields) implements Type {}
            record Array(Type type, int count) implements Type {}

            enum Raw implements Type {
                FLOAT, FLOAT2, FLOAT3, FLOAT4,
                INT, INT2, INT3, INT4,
                UINT, UINT2, UINT3, UINT4,
                MAT2x2, MAT2x3, MAT2x4,
                MAT3x2, MAT3x3, MAT3x4,
                MAT4x2, MAT4x3, MAT4x4,
            }
        }

    }

}
