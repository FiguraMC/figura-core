package org.figuramc.figura_core.minecraft_interop;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All item rendering contexts.
 * The client will create them and give them names.
 */
public class ItemRenderContext {

    public static final Map<String, ItemRenderContext> CONTEXTS_BY_NAME = new ConcurrentHashMap<>();

    // Name for this context, so we can recognize it
    public final String name;
    // Whether to mirror placement (translation/rotation, not scale) across the X axis
    public final boolean mirrorPlacement;
    // Whether to use the "extruded png" format by default, or the item model format.
    public final boolean defaultToExtrudedPng;
    // If this context is not present, use this as a fallback and flip across the YZ plane.
    // This is used for left/right handedness defaults.
    public final @Nullable ItemRenderContext fallback;

    public ItemRenderContext(String name, boolean mirrorPlacement, boolean defaultToExtrudedPng, @Nullable ItemRenderContext fallback) {
        this.name = name;
        this.mirrorPlacement = mirrorPlacement;
        this.defaultToExtrudedPng = defaultToExtrudedPng;
        this.fallback = fallback;
        if (CONTEXTS_BY_NAME.putIfAbsent(name, this) != null)
            throw new IllegalArgumentException("Duplicate ItemRenderContext name: \"" + name + "\"");
    }

}
