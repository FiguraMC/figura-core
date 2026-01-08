package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.util.enumlike.EnumLike;

public class EquipmentSlot extends EnumLike {

    public EquipmentSlot(String name) {
        super(name);
    }

    public static final EquipmentSlot MAINHAND = new EquipmentSlot("MAINHAND");
    public static final EquipmentSlot OFFHAND = new EquipmentSlot("OFFHAND");
    public static final EquipmentSlot FEET = new EquipmentSlot("FEET");
    public static final EquipmentSlot LEGS = new EquipmentSlot("LEGS");
    public static final EquipmentSlot CHEST = new EquipmentSlot("CHEST");
    public static final EquipmentSlot HEAD = new EquipmentSlot("HEAD");
    public static final EquipmentSlot BODY = new EquipmentSlot("BODY");
    public static final EquipmentSlot SADDLE = new EquipmentSlot("SADDLE");

}
