package org.figuramc.figura_core.minecraft_interop.game_data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ability to manipulate a Minecraft identifier
 * This is mainly used to indicate that's what it is in the type system, instead of just using String
 * @param namespace
 * @param name
 */
public record MinecraftIdentifier(String namespace, String name) implements Comparable<MinecraftIdentifier> {

    // Attempt to parse the identifier from the given info.
    // Expects exactly 0 or 1 delimiter. If there's more than 1, returns null.
    // If there's 1 delimiter, the namespace is everything before it, and the name is everything after it.
    // If there is no delimiter, the string is treated as the name, and the default namespace "minecraft" is assigned.
    public static @Nullable MinecraftIdentifier parse(String string) {
        return parse(string, ':');
    }
    public static @Nullable MinecraftIdentifier parse(String ident, char delimiter) {
        int index = ident.indexOf(delimiter);
        if (index == -1) return new MinecraftIdentifier("minecraft", ident);
        int lastIndex = ident.lastIndexOf(delimiter);
        if (index != lastIndex) return null;
        return new MinecraftIdentifier(ident.substring(0, index), ident.substring(index+1));
    }


    public @NotNull String toString() { return namespace + ":" + name; }

    @Override
    public int compareTo(@NotNull MinecraftIdentifier o) {
        int namespaceCompare = namespace.compareTo(o.namespace);
        if (namespaceCompare != 0) return namespaceCompare;
        return name.compareTo(o.name);
    }
}