package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemStackView<T extends MinecraftItemStack> extends SimpleView<T> implements CallbackItem {


    public ItemStackView(@NotNull T itemStack) { super(itemStack); }
    public ItemStackView(@NotNull T itemStack, AbstractView parent) { super(itemStack, parent); }

}
