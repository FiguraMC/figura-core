package org.figuramc.figura_core.minecraft_interop.game_data.block;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.List;

/**
 * Wraps a minecraft BlockState, as well as a Vector3i indicating the position.
 * This is useful because many block properties can depend on the position in the world.
 */
public interface MinecraftBlockState {

    MinecraftIdentifier getIdentifier();

    // Get the position bundled alongside the blockstate
    // Store the value in the param and return it, like JOML
    Vector3i getBundledPos(Vector3i output);
    // Return a new MinecraftBlockState with the same BlockState as this one, but with the given bundled pos
    MinecraftBlockState withBundledPos(Vector3ic position);

    // List of AABBs, denoted by their corners
    record AABB(double x1, double y1, double z1, double x2, double y2, double z2) {}
    List<AABB> getCollisionShape();
    List<AABB> getOutlineShape();

//    HashMap<String, Set<String>> getTextures(); // TODO make this return Set<MinecraftTexture> instead. Also make the keys be a Direction enum instead of strings
//    Map<String, Object> getSounds(); // TODO make this once we have a MinecraftSound handle object. Also make it a custom class instead of Map<String, Object>
    List<String> getProperties();
    List<MinecraftIdentifier> getTags();
    List<MinecraftIdentifier> getFluidTags();

    // TODO NBT handling
    // Object? getEntityData

    // Write output to the vector and return it, like JOML
    Vector3f getMapColor(Vector3f output);

    MinecraftItemStack asItem();

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
