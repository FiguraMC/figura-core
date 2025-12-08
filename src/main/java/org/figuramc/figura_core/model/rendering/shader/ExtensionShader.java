package org.figuramc.figura_core.model.rendering.shader;

import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexElem;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.SetUtils;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.figuramc.figura_core.util.enumlike.IdMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * An Extension Shader is a shader which is based off another shader,
 * plus additional customizations added to it.
 */
public final class ExtensionShader implements FiguraShader {

    public final BuiltinShader base; // The base shader which this is extending and modifying
    public final FiguraVertexFormat vertexFormat; // Vertex format, including extra vertex elements
    public final List<String> textureBindingPoints; // All texture binding points, including base ones
    public final IdMap<ShaderHookPoint, @Nullable String> hookImplementations; // Implementations of shader hooks

    // Throws an IllegalArgumentException if something is incorrect in configuring the shader
    public ExtensionShader(BuiltinShader base, List<Pair<String, FiguraVertexElem>> extraVertexElems, List<String> extraTextureBindingPoints, IdMap<ShaderHookPoint, String> hookImplementations) throws IllegalArgumentException {
        this.base = base;
        // Will error if there are any duplicate vertex elements
        this.vertexFormat = FiguraVertexFormat.extend(base.vertexFormat(), extraVertexElems);
        // Ensure there's no duplicate texture names
        if (!Collections.disjoint(base.textureBindingPoints(), extraTextureBindingPoints))
            throw new IllegalArgumentException("Duplicate texture names: " + SetUtils.intersection(new HashSet<>(base.textureBindingPoints), new HashSet<>(extraTextureBindingPoints)));
        this.textureBindingPoints = ListUtils.concat(base.textureBindingPoints(), extraTextureBindingPoints);
        // Set up hooks
        // TODO: Verify these in some sense; some kind of sandboxing pass parsing it to ensure no giant/infinite loops would be good!
        this.hookImplementations = hookImplementations;
    }

    @Override public FiguraVertexFormat vertexFormat() { return vertexFormat; }
    @Override public List<String> textureBindingPoints() { return textureBindingPoints; }

}
