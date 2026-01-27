package org.figuramc.figura_core.model.rendering.shader;

import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;

import java.util.List;

import static org.figuramc.figura_core.data.materials.ModuleMaterials.BuiltinTextureBinding.*;

public enum BuiltinShader implements FiguraShader {

    ALBEDO(FiguraVertexFormat.ALBEDO, List.of("Albedo", "LightMap"), List.of(NONE, LIGHTMAP)),
    ALBEDO_NORMAL(FiguraVertexFormat.ALBEDO_NORMAL, List.of("Albedo", "Normal", "LightMap"), List.of(NONE, NONE, LIGHTMAP)),
    ALBEDO_SPECULAR(FiguraVertexFormat.ALBEDO_SPECULAR, List.of("Albedo", "Specular", "LightMap"), List.of(NONE, NONE, LIGHTMAP)),
    ALBEDO_NORMAL_SPECULAR(FiguraVertexFormat.ALBEDO_NORMAL_SPECULAR, List.of("Albedo", "Normal", "Specular", "LightMap"), List.of(NONE, NONE, NONE, LIGHTMAP)),
    END_PORTAL(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2"), List.of(NONE, NONE)),
    END_GATEWAY(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2"), List.of(NONE, NONE)),

    TEXT_SHADER(FiguraVertexFormat.TEXT_SHADER, List.of("Albedo", "LightMap"), List.of(NONE, LIGHTMAP)),

    ;

    public final FiguraVertexFormat vertexFormat;
    public final List<String> textureBindingPoints;
    public final List<ModuleMaterials.BuiltinTextureBinding> defaultBindings;

    BuiltinShader(FiguraVertexFormat vertexFormat, List<String> textureBindingPoints, List<ModuleMaterials.BuiltinTextureBinding> defaultBindings) {
        this.vertexFormat = vertexFormat;
        this.textureBindingPoints = textureBindingPoints;
        this.defaultBindings = defaultBindings;
    }

    @Override public FiguraVertexFormat vertexFormat() { return vertexFormat; }
    @Override public List<String> textureBindingPoints() { return textureBindingPoints; }
    @Override public List<ModuleMaterials.BuiltinTextureBinding> defaultBindings() { return defaultBindings; }
}
