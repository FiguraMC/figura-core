package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MinecraftItemStack {

    MinecraftIdentifier getId();
    List<MinecraftIdentifier> getTags();

    // TODO: Conversion to a block is tricky since a MinecraftBlockState requires a world.
    //       Should items have an attached world? Should we take a world as a param when converting item to block?

    String getName();
    String getStackString(); // No real exact definition atm, only really useful for debug stuff...

    ItemUseAction getUseAction();
    ItemRarity getRarity();
    @Nullable EquipmentSlot getEquipmentSlot(); // TODO Should we make this nullable, or have a "none" slot?

    int getCount();
    int getDurability();
    int getMaxDurability();
    int getPopTime(); // Do we need this?
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
