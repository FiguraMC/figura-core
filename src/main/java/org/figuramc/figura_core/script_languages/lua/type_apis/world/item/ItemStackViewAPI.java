package org.figuramc.figura_core.script_languages.lua.type_apis.world.item;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.EquipmentSlot;
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
    public static LuaTable getTags(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return s.listToTable(
                fetchItemStack(s, self).getTags(),
                (r, tag) -> LuaString.valueOf(r.allocationTracker, tag.toString())
        );
    }

//    @LuaExpose @LuaPassState
//    public static ItemStackView<?> copy(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM {
//        return new ItemStackView<>(fetchItemStack(s, self).copy());
//    }

    public static BlockStateView<?> blockState(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM {
        MinecraftBlockState blockState = fetchItemStack(s, self).getBlockState();
        return blockState == null ? null : new BlockStateView<>(blockState, self);
    }

    @LuaExpose @LuaPassState
    public static String useAction(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getUseAction().name;
    }

    @LuaExpose @LuaPassState
    public static String name(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getName();
    }

    @LuaExpose @LuaPassState
    public static String getIdentifier(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getIdentifier().toString();
    }

    @LuaExpose @LuaPassState
    public static String getRarity(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getRarity().name;
    }

    // TODO: debate on NBT data
    @LuaExpose @LuaPassState
    public static String toStackString(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).toStackString();
    }

    @LuaExpose @LuaPassState
    public static @Nullable String equipmentSlot(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        EquipmentSlot e = fetchItemStack(s, self).getEquipmentSlot();
        return e == null ? null : e.name;
    }

    @LuaExpose @LuaPassState
    public static int count(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getCount();
    }

    @LuaExpose @LuaPassState
    public static int damage(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getDamage();
    }

    @LuaExpose @LuaPassState
    public static int popTime(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getPopTime();
    }

    @LuaExpose @LuaPassState
    public static int maxDamage(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getMaxDamage();
    }

    @LuaExpose @LuaPassState
    public static int repairCost(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getRepairCost();
    }

    @LuaExpose @LuaPassState
    public static int useDuration(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).getUseDuration();
    }

    @LuaExpose @LuaPassState
    public static boolean hasGlint(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).hasGlint();
    }

    @LuaExpose @LuaPassState
    public static boolean isBlockItem(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isBlockItem();
    }

    @LuaExpose @LuaPassState
    public static boolean isFood(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isFood();
    }

    @LuaExpose @LuaPassState
    public static boolean isEnchantable(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isEnchantable();
    }

    @LuaExpose @LuaPassState
    public static boolean isDamageable(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isDamageable();
    }

    @LuaExpose @LuaPassState
    public static boolean isStackable(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isStackable();
    }

    @LuaExpose @LuaPassState
    public static boolean isArmor(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isArmor();
    }

    @LuaExpose @LuaPassState
    public static boolean isTool(LuaState s, ItemStackView<?> self) throws LuaError, LuaOOM {
        return fetchItemStack(s, self).isTool();
    }

    // Helper to fetch entity, or error if revoked
    private static @NotNull MinecraftItemStack fetchItemStack(LuaState state, ItemStackView<?> itemStackView) throws LuaError, LuaOOM {
        // Get itemStack
        @Nullable MinecraftItemStack itemStack = itemStackView.getValue();
        // If null (aka revoked), error
        if (itemStack == null) throw new LuaError("Attempt to use item stack view after it was revoked!", state.allocationTracker);
        // Return the non-null itemStack
        return itemStack;
    }
}
