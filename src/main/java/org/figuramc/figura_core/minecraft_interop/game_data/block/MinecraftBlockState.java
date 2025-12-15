package org.figuramc.figura_core.minecraft_interop.game_data.block;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.joml.Vector3d;

import java.util.*;

/**
 *   A view into a MinecraftBlockState instance
 */
public interface MinecraftBlockState {

    String getId();

    Vector3d getPos();

    List<List<Vector3d>> getCollisionShape();
    List<List<Vector3d>> getOutlineShape();

    HashMap<String, Set<String>> getTextures();
    Map<String, Object> getSounds();
    List<String> getProperties();
    List<String> getTags();
    List<String> getFluidTags();

    // TODO NBT handling
    // Object? getEntityData

    Vector3d getMapColor();

    MinecraftItem asItem();

    String toStateString();

    int getOpacity();
    int getComparatorOutput(String direction);
    int getLuminance();
    float getHardness();
    float getFriction();
    float getVelocityModifier();
    float getJumpVelocityModifier();
    float getBlastResistance();

    boolean isTranslucent();
    boolean isSolidBlock();
    boolean isFullCube();
    boolean hasEmissiveLighting();
    boolean hasBlockEntity();
    boolean isOpaque();
    boolean emitsRedstonePower();
    boolean hasCollision();
    boolean isAir();
}
