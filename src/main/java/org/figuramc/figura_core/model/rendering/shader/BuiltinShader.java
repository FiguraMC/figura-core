package org.figuramc.figura_core.model.rendering.shader;

import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;

import java.util.List;

import static org.figuramc.figura_core.data.materials.ModuleMaterials.BuiltinTextureBinding.*;

public enum BuiltinShader implements FiguraShader {

    BASIC(FiguraVertexFormat.DEFAULT, List.of("Main", "NormalMap", "SpecularMap", "LightMap"), List.of(NONE, NONE, NONE, LIGHTMAP)),
    END_PORTAL(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2"), List.of(NONE, NONE)),
    END_GATEWAY(FiguraVertexFormat.POSITION, List.of("Tex1", "Tex2"), List.of(NONE, NONE)),

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
