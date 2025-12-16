package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemStackView<T extends MinecraftItemStack> implements CallbackItem, AutoCloseable {

    private @Nullable T itemStack;

    public ItemStackView(@NotNull T itemStack) {
        this.itemStack = itemStack;
    }

    // Return the entity, or null if revoked.
    public synchronized @Nullable T getItemStack() {
        return itemStack;
    }

    // Revoke the entity view.
    @Override
    public synchronized void close() {
        itemStack = null;
    }
}
