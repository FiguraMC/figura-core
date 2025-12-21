package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MinecraftItemStack {

    MinecraftIdentifier getIdentifier(); // Identifier for the item type

    List<MinecraftIdentifier> getTags();
    // maybe? List<String> getEnchantments();

//    MinecraftItemStack copy(); // TODO should this exist? What use is there in an ideal world to "copy" an item stack? We also don't know how much memory this might use.
    MinecraftBlockState getBlockState();

    // TODO: NBT DISCUSSION
//    Object getTag();

    ItemUseAction getUseAction();
    String getName();
    ItemRarity getRarity();
    String toStackString();
    @Nullable EquipmentSlot getEquipmentSlot();

    int getCount();
    int getDamage();
    int getPopTime();
    int getMaxDamage();
    int getRepairCost();
    int getUseDuration();

    boolean hasGlint();
    boolean isBlockItem();
    boolean isFood();
    boolean isEnchantable();
    boolean isDamageable();
    boolean isStackable();
    boolean isArmor();
    boolean isTool();
}
