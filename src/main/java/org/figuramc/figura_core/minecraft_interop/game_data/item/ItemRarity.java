package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.util.enumlike.EnumLike;

public class ItemRarity extends EnumLike {

    public ItemRarity(String name) {
        super(name);
    }

    public static final ItemRarity COMMON = new ItemRarity("COMMON");
    public static final ItemRarity UNCOMMON = new ItemRarity("UNCOMMON");
    public static final ItemRarity RARE = new ItemRarity("RARE");
    public static final ItemRarity EPIC = new ItemRarity("EPIC");

}
