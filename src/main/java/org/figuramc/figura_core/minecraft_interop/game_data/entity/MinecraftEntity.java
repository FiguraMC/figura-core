package org.figuramc.figura_core.minecraft_interop.game_data.entity;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.item.EquipmentSlot;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.minecraft_interop.game_data.types.AABB;
import org.figuramc.figura_core.minecraft_interop.game_data.world.MinecraftWorld;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

/**
 * A view into a Minecraft Entity instance.
 * TODO look for any unsynced things and move them out of here
 */
public interface MinecraftEntity {

    // Figura use, not for scripts (sorry)
    VanillaModel getModel();
    boolean isGone();

    // Get the world this entity is part of
    MinecraftWorld getWorld();

    // Identity
    String getName();
    UUID getUUID();

    // Type queries
    MinecraftIdentifier getType(); // "minecraft:pig" for example
    boolean isPlayer();
    boolean isLivingEntity(); // Loose qualifier for some definition of "living", probably generally correct
    boolean hasInventory(); // If this entity has an inventory (not sure what the point of this is tbh)

    // Space
    Vector3d getPosition(float tickDelta, Vector3d out);
    Vector2f getRotation(float tickDelta, Vector2f out);
    Vector3d getVelocity(Vector3d out); // pos(1) - pos(0)
    Vector3d getLookDirection(float tickDelta, Vector3d out);
    double getEyeHeight(); // How high this entity's eyes are above its position
    AABB getBoundingBox();

    // Status
    float getFreezeTime(); // Time frozen
    float getFreezeDuration(); // Maximum time frozen before freezing
    float getAir(); // Number of ticks of air remaining
    float getMaxAir(); // Maximum air capacity
    float getHealth(); // Current health
    float getMaxHealth(); // Maximum health
    float getArmor();
    float getAbsorption();
    float getDyingTime(); // Time in dying animation
    float getDyingDuration(); // Max time in dying animation before it ends

    boolean isGlowing();
    boolean isInvisible();
    boolean isSilent();
    boolean isOnFire();
    int getArrowCount(); // Number of arrows stuck in the entity
    int getStingerCount(); // Number of bee stingers stuck in the entity

    // Movement state
    EntityPose getPose();
    boolean isSneaking();
    boolean isSprinting();
    boolean isMoving();
    boolean isMovingHorizontally();
    boolean isOnGround();
    boolean isFalling();
    boolean isClimbing();
    boolean isGliding();
    boolean isBlocking();
    boolean isSwimming(); // Swimming and crawling are the same pose, just whether you're in water
    boolean isCrawling();
    boolean isRiptideSpinning();

    // Hand
    boolean isLeftHanded();
    boolean isSwinging();
    float getSwingTime();
    float getSwingDuration();
    boolean swingingOffHand();

    // Visual
    boolean isSlim();

//    float getFood();
//    float getMaxFood();
//    float getSaturation();
//    float getExhaustion();

    // Fluid / world state
    boolean isInWater();
    boolean isUnderwater();
    boolean isInLava();
    boolean isInRain();

    // Riding
    @Nullable MinecraftEntity getVehicle();
    @Nullable MinecraftEntity getControlledVehicle();
    List<MinecraftEntity> getPassengers();
    @Nullable MinecraftEntity getControllingPassenger();

    // Inventory/items stuff(?)
    @Nullable MinecraftItemStack getItem(EquipmentSlot slot); // Get the item in the given equipment slot, if any
    @Nullable MinecraftItemStack getActiveItem(); // The "active" item, if any
    int getItemUseTicks(); // The number of ticks the entity's been using the active item for
    boolean isFishing();

}