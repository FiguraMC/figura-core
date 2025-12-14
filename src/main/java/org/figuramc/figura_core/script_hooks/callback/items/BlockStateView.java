package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A view of a Minecraft Block.
 */
public final class BlockStateView<T extends MinecraftBlockState> implements CallbackItem, AutoCloseable {

    private @Nullable T blockState;

    public BlockStateView(@NotNull T blockState) { this.blockState = blockState; }

    public synchronized @Nullable T getBlockState() { return blockState; }

    // Revoke the BlockStateView
    @Override
    public synchronized void close() { blockState = null; }
}
