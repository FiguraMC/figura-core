package org.figuramc.figura_core.minecraft_interop.vanilla_parts;

import org.jetbrains.annotations.Nullable;

/**
 * An interface that lets us see data from a (logical) Vanilla Part.
 * In older versions of Minecraft, the same ModelPart instance was
 * rendered multiple times in one frame at different locations.
 * While this is the same java instance, we would treat these locations as
 * separate LOGICAL VanillaPart instances.
 * <p>
 * VanillaPart instances are not separate per Avatar.
 * Each logical VanillaPart is essentially a singleton, read and written to
 * by multiple avatars during the game rendering process.
 * <p>
 * VanillaParts also uphold a logical hierarchy.
 * When part A is specified as an ancestor of part B,
 * transformations applied to part A should be reflected in part B as well.
 */
public abstract class VanillaPart {
    /**
     * Get the parent of this part, if any.
     * Certain parts are specified to be children of other parts;
     * this means that transforms are propagated from parent to child.
     */
    public abstract @Nullable VanillaPart parent();
}
