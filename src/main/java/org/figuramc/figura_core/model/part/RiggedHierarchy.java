package org.figuramc.figura_core.model.part;

import org.jetbrains.annotations.Nullable;

/**
 * A tree of T, where each element can be transformed. This allows animations to bind to it.
 * This allows for code re-use between FiguraModelPart and VanillaPart.
 * Operations:
 * - The thing must have a transform
 * - It must optionally be able to get its descendants by name (This is for binding animations to it)
 * @param <T> Must be this type itself!
 */
public interface RiggedHierarchy<T extends RiggedHierarchy<T>> {
    // The object has a transform
    PartTransform getTransform();
    // Get child with given name, if any
    @Nullable T getChildByName(String name);

    // Look for a descendant with the given path, separated with slashes.
    // If the path is the empty string "", should return this.
    // If there is no part at the given path, return null.
    @SuppressWarnings("unchecked") // Cast will succeed if the implementor successfully set T = implementor
    default @Nullable T getDescendantWithPath(String path) {
        RiggedHierarchy<T> part = this;
        // TODO maybe optimize this?
        for (String pathElement : path.split("/")) {
            part = part.getChildByName(pathElement);
            if (part == null) return null;
        }
        return (T) part;
    }
}
