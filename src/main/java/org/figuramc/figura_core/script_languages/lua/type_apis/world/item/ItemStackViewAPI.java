package org.figuramc.figura_core.script_languages.lua.type_apis.world.item;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.ItemStackView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@LuaTypeAPI(typeName = "ItemStack", wrappedClass = ItemStackView.class)
public class ItemStackViewAPI {

    public static LuaUserdata wrap(ItemStackView<?> itemStack, LuaRuntime state) {
        return new LuaUserdata(itemStack, state.figuraMetatables.entityView);
    }

    // Helper to fetch entity, or error if revoked
    private static @NotNull MinecraftItemStack fetchItemStack(LuaState state, ItemStackView<?> itemStackView) throws LuaError, LuaUncatchableError {
        // Get entity
        @Nullable MinecraftItemStack itemStack = itemStackView.getItemStack();
        // If null (aka revoked), error
        if (itemStack == null) throw new LuaError("Attempt to use entity view after it was revoked!", state.allocationTracker);
        // Return the non-null entity
        return itemStack;
    }
}
