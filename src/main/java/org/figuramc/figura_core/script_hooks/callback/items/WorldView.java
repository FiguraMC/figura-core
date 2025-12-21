package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A view into a Minecraft World
 */
public final class WorldView<T extends MinecraftWorld> extends SimpleView<T> implements CallbackItem {

    public WorldView(@NotNull T world) { super(world); }
    public WorldView(@NotNull T world, AbstractView parent) { super(world, parent); }

}
