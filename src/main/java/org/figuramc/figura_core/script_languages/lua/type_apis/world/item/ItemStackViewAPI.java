package org.figuramc.figura_core.script_languages.lua.type_apis.world.item;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDynamicField;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.item.EquipmentSlot;
import org.figuramc.figura_core.minecraft_interop.game_data.item.ItemRarity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.ItemUseAction;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.script_hooks.callback.items.ItemStackView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@LuaTypeAPI(typeName = "ItemStack", wrappedClass = ItemStackView.class)
public class ItemStackViewAPI {

    public static LuaUserdata wrap(ItemStackView<?> itemStack, LuaRuntime state) {
        return new LuaUserdata(itemStack, state.figuraMetatables.itemStackView);
    }

    @LuaExpose @LuaPassState @LuaDynamicField public static String id(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getId().toString(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable tags(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return s.stringifyListToTable(fetchItemStack(s, self).getTags()); }

    @LuaExpose @LuaPassState @LuaDynamicField public static String name(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getName(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static String stackString(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getStackString(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static ItemUseAction useAction(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getUseAction(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static ItemRarity rarity(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getRarity(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable EquipmentSlot slot(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getEquipmentSlot(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static int count(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getCount(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int durability(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getDurability(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int maxDurability(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getMaxDurability(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int popTime(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getPopTime(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int repairCost(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getRepairCost(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int useDuration(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).getUseDuration(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean hasGlint(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).hasGlint(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isBlockItem(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isBlockItem(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isFood(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isFood(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isEnchantable(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isEnchantable(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isDamageable(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isDamageable(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isStackable(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isStackable(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isArmor(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isArmor(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isTool(LuaRuntime s, ItemStackView<?> self) throws LuaError, LuaOOM { return fetchItemStack(s, self).isTool(); }

    // Helper to fetch item, or error if revoked
    private static @NotNull MinecraftItemStack fetchItemStack(LuaState state, ItemStackView<?> itemStackView) throws LuaError, LuaOOM {
        // Get itemStack
        @Nullable MinecraftItemStack itemStack = itemStackView.getValue();
        // If null (aka revoked), error
        if (itemStack == null) throw new LuaError("Attempt to use item stack view after it was revoked!", state.allocationTracker);
        // Return the non-null itemStack
        return itemStack;
    }

}
