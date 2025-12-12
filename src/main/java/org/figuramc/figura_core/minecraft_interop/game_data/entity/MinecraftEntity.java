package org.figuramc.figura_core.minecraft_interop.game_data.entity;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

/**
 * A view into a Minecraft Entity instance, with required operations.
 */
public interface MinecraftEntity {

    // Return the kind of entity this is. Used as a key for CEM.
    EntityKind getKind();

    // Get the UUID of this entity.
    UUID getUUID();

    // Get the vanilla model instance for this entity.
    // Generally this should be shared between all entities of the same Kind, though this is not necessary.
    VanillaModel getModel();

    // Whether this entity is "gone" and we should stop referring to it.
    boolean isGone();

    // Various getters for script purposes.
    Vector3d getPosition(float tickDelta, Vector3d output); // Fill output with the info and return it.
    Vector2d getRotation(float tickDelta, Vector2d output);
    Vector3d getVelocity(Vector3d output);
    Vector3d getLookDir(Vector3d output);

    @Nullable
    MinecraftEntity getVehicle();
    @Nullable
    MinecraftEntity getControlledVehicle();
    @Nullable
    List<MinecraftEntity> getPassengers();
    @Nullable
    MinecraftEntity getControllingPassenger();

    // Should return [MinecraftEntity, Vec3]
    @Nullable
    Object[] getTargetedEntity(Double distance);
    @Nullable
    MinecraftEntity getNearestEntity(String type, Double radius);

    String getName();
    String getType();
    String getDimensionName();
    String getPose();

    int getFrozenTicks();
    int getMaxAir();
    float getEyeHeight();

    // State checks
    boolean isPlayer();
    boolean isCrouching();
    boolean isSprinting();
    boolean isMoving(boolean ignoreY);
    boolean isOnGround();
    boolean isFalling();
    boolean isWet();
    boolean isInWater();
    boolean isInLava();
    boolean isInRain();
    boolean isUnderWater();
    boolean isGlowing();
    boolean isInvisible();
    boolean isSilent();
    boolean isOnFire();
    boolean isAlive();
    boolean hasInventory();

    // TODO: Define what type to use for NBT
    Object getNBT();
    // TODO:  Minecraft Block API
    //MinecraftBlock? getTargetedBlock(boolean ignoreLiquids, float distance);


    // Ours
    boolean hasAvatar();
    int getPermissionLevel();
    Object getVariable(String key);
}