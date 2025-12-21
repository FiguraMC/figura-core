package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

/**
 * A view of a Minecraft BlockState.
 */
public final class BlockStateView<T extends MinecraftBlockState> extends SimpleView<T> implements CallbackItem {

    public BlockStateView(@NotNull T blockState) { super(blockState); }
    public BlockStateView(@NotNull T blockState, AbstractView parent) { super(blockState, parent); }

}
