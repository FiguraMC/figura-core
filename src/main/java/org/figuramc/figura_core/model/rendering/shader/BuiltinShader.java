package org.figuramc.figura_core.model.rendering.shader;

import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;

import java.util.List;

public enum BuiltinShader implements FiguraShader {

    BASIC(FiguraVertexFormat.DEFAULT, List.of("Main", "NormalMap", "SpecularMap", "LightMap")),
    END_PORTAL(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2")),
    END_GATEWAY(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2")),

    ;

    public final FiguraVertexFormat vertexFormat;
    public final List<String> textureBindingPoints;

    BuiltinShader(FiguraVertexFormat vertexFormat, List<String> textureBindingPoints) {
        this.vertexFormat = vertexFormat;
        this.textureBindingPoints = textureBindingPoints;
    }

    @Override public FiguraVertexFormat vertexFormat() { return vertexFormat; }
    @Override public List<String> textureBindingPoints() { return textureBindingPoints; }
}
