package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A view into a Minecraft Level
 */
public final class WorldView<T extends MinecraftWorld> implements CallbackItem, AutoCloseable {

    private @Nullable T world;

    public WorldView(@NotNull T world) { this.world = world; }

    public synchronized @Nullable T getWorld() { return world; }

    @Override
    public synchronized void close() { world = null; }
}
