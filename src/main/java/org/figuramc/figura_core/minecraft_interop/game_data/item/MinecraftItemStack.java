package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MinecraftItemStack {

    List<String> getTags();
    // maybe? List<String> getEnchantments();

    MinecraftItemStack copy();
    MinecraftBlockState getBlockState();

    /**
     * Get the item type which this is a stack of
     */
    MinecraftItem getItem();

    // TODO: IDK what this means yet
    Object getTag();

    String getUseAction();
    String getName();
    String getID();
    String getRarity();
    String toStackString();
    @Nullable
    String getEquipmentSlot();

    int getCount();
    int getDamage();
    int getPopTime();
    int getMaxDamage();
    @Nullable
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
