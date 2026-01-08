package org.figuramc.figura_core.minecraft_interop.game_data.block;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.minecraft_interop.game_data.types.AABB;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;

/**
 * Wraps a minecraft BlockState, a world, and a position.
 * This is useful because many block properties can depend on the position in the world.
 */
public interface MinecraftBlockState {

    // Splitting these into vague categories so I feel better

    // Instance
    MinecraftBlockState withPos(int x, int y, int z); // This same block state but with a different position
    MinecraftIdentifier getId();
    Vector3i getPos(Vector3i output); // Store to the output and return it, like JOML functions
    MinecraftItemStack asItem(); // This block but as an item, or air if there is no item

    // Shape/collision
    List<AABB> getCollisionShape();
    List<AABB> getOutlineShape();
    boolean hasCollision(); // Whether this block has any collision at all
    boolean isFullCube(); // Whether this block's collision takes up an entire block of space
    boolean isOpaque(); // Whether this block can visually occlude others (block face culling stuff)

    // State
    List<String> getProperties(); // Names of properties, not their values
    List<MinecraftIdentifier> getTags();
    List<MinecraftIdentifier> getFluidTags();
    boolean hasBlockEntity(); // Whether there's a block entity containing extra custom data
    String getStateString(); // This doesn't have an exact definition across versions. Is this just for debugging?

    // Redstone?
    boolean conductsRedstone(); // Stone yes, glass no

    // Movement
    float getFriction(); // Detect icy surfaces (and slime blocks apparently)
    float getVelocityModifier(); // Detect slow-y surfaces (soulsand & honey)
    float getJumpModifier(); // Detect sticky surfaces (honey)

    // Breaking
    float getHardness();
    float getBlastResistance();

    // Visuals
    Vector3f getMapColor(Vector3f out);

    // Light
    float getLightBlocked(); // The amount of light blocked by this (usually 0 or 1)
    float getLightEmitted(); // The amount of light emitted by this block (0 to 1)
    boolean usesEmissiveRendering(); // Whether this is locked at 100% brightness for rendering (like magma)


}
