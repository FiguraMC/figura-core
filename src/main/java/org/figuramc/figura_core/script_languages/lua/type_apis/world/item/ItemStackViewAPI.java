package org.figuramc.figura_core.script_languages.lua.type_apis.world.item;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.script_hooks.callback.items.BlockStateView;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.ItemStackView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@LuaTypeAPI(typeName = "ItemStack", wrappedClass = ItemStackView.class)
public class ItemStackViewAPI {

    public static LuaUserdata wrap(ItemStackView<?> itemStack, LuaRuntime state) {
        return new LuaUserdata(itemStack, state.figuraMetatables.itemStackView);
    }

    @LuaExpose @LuaPassState
    public static LuaTable getTags(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        List<String> tags = fetchItemStack(s, self).getTags();
        LuaTable table = new LuaTable(tags.size(), 1, s.allocationTracker);

        int i = 1;
        for (String tag : tags) {
            // Convert each MinecraftEntity into an EntityViewAPI
            table.rawset(i, LuaString.valueOf(s.allocationTracker, tag));
            i++;
        }
        return table;
    }

    @LuaExpose @LuaPassState
    public static ItemStackView<?> copy(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return new ItemStackView<>(fetchItemStack(s, self).copy());
    }

    public static BlockStateView<?> blockState(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        MinecraftBlockState blockState = fetchItemStack(s, self).getBlockState();
        if (blockState == null)
            return null;
        return new BlockStateView<>(blockState);
    }

    @LuaExpose @LuaPassState
    public static String useAction(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getUseAction();
    }

    @LuaExpose @LuaPassState
    public static String name(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getName();
    }

    @LuaExpose @LuaPassState
    public static String getID(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getID();
    }

    @LuaExpose @LuaPassState
    public static String getRarity(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getRarity();
    }

    // TODO: debate on NBT data
    @LuaExpose @LuaPassState
    public static String toStackString(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).toStackString();
    }

    @LuaExpose @LuaPassState
    public static String equipmentSlot(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getEquipmentSlot();
    }

    @LuaExpose @LuaPassState
    public static int count(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getCount();
    }

    @LuaExpose @LuaPassState
    public static int damage(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getDamage();
    }

    @LuaExpose @LuaPassState
    public static int popTime(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getPopTime();
    }

    @LuaExpose @LuaPassState
    public static int maxDamage(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getMaxDamage();
    }

    @LuaExpose @LuaPassState
    public static int repairCost(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getRepairCost();
    }

    @LuaExpose @LuaPassState
    public static int useDuration(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).getUseDuration();
    }

    @LuaExpose @LuaPassState
    public static boolean hasGlint(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).hasGlint();
    }

    @LuaExpose @LuaPassState
    public static boolean isBlockItem(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isBlockItem();
    }

    @LuaExpose @LuaPassState
    public static boolean isFood(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isFood();
    }

    @LuaExpose @LuaPassState
    public static boolean isEnchantable(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isEnchantable();
    }

    @LuaExpose @LuaPassState
    public static boolean isDamagable(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isDamageable();
    }

    @LuaExpose @LuaPassState
    public static boolean isStackable(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isStackable();
    }

    @LuaExpose @LuaPassState
    public static boolean isArmor(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isArmor();
    }

    @LuaExpose @LuaPassState
    public static boolean isTool(LuaState s, ItemStackView<?> self) throws LuaError, LuaUncatchableError {
        return fetchItemStack(s, self).isTool();
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
