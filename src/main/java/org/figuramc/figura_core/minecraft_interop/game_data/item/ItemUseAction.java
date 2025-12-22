package org.figuramc.figura_core.minecraft_interop.game_data.item;

import org.figuramc.figura_core.util.enumlike.EnumLike;

public class ItemUseAction extends EnumLike {

    public final String name;
    public ItemUseAction(String name) {
        this.name = name;
    }

    public static final ItemUseAction NONE = new ItemUseAction("NONE");
    public static final ItemUseAction EAT = new ItemUseAction("EAT");
    public static final ItemUseAction DRINK = new ItemUseAction("DRINK");
    public static final ItemUseAction BLOCK = new ItemUseAction("BLOCK");
    public static final ItemUseAction BOW = new ItemUseAction("BOW");
    public static final ItemUseAction TRIDENT = new ItemUseAction("TRIDENT");
    public static final ItemUseAction CROSSBOW = new ItemUseAction("CROSSBOW");
    public static final ItemUseAction SPYGLASS = new ItemUseAction("SPYGLASS");
    public static final ItemUseAction TOOT_HORN = new ItemUseAction("TOOT_HORN");
    public static final ItemUseAction BRUSH = new ItemUseAction("BRUSH");
    public static final ItemUseAction BUNDLE = new ItemUseAction("BUNDLE");
    public static final ItemUseAction SPEAR = new ItemUseAction("SPEAR");

    @Override
    @Deprecated
    public String toString() {
        return "ItemUseAction[" + name + "]";
    }

}
