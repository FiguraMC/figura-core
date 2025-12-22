package org.figuramc.figura_core.minecraft_interop.game_data.entity;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

/**
 * A view into a Minecraft Entity instance, with required operations.
 * equals() implementation should check if the underlying entity is the same!
 */
public interface MinecraftEntity {

    // Get the UUID of this entity.
    UUID getUUID();

    // Get the vanilla model instance for this entity.
    // Generally this should be shared between all entities of the same Kind, though this is not necessary.
    VanillaModel getModel();

    // Whether this entity is "gone" and we should stop referring to it.
    boolean isGone();

    // Various getters for script purposes.
    // Fill the "output" param with data and return it, like the JOML functions do
    Vector3d getPosition(float tickDelta, Vector3d output);
    Vector2f getRotation(float tickDelta, Vector2f output); // Return values are in degrees, in Minecraft's angle/coordinate system

    // Convenient default implementations for these functions in terms of others
    // Client can implement them separately if it wants to be more efficient
    default Vector3d getVelocity(Vector3d output) { return getPosition(1, output).sub(getPosition(0, new Vector3d())); }
    default Vector3d getLookDirection(float tickDelta, Vector3d output) {
        Vector2f rot = getRotation(tickDelta, new Vector2f());
        double pitch = rot.x * -MathUtils.DEG_TO_RAD; // Negate angles because of weird coordinate system
        double yaw = rot.y * -MathUtils.DEG_TO_RAD;
        double y = Math.sin(pitch);
        double horizontalScale = Math.cos(pitch);
        double z = Math.cos(yaw) * horizontalScale; // Z is cos because of weird coordinate system
        double x = Math.sin(yaw) * horizontalScale; // X is sin because of weird coordinate system
        return output.set(x, y, z);
    }

    @Nullable MinecraftEntity getVehicle();
    @Nullable MinecraftEntity getControlledVehicle();
    List<MinecraftEntity> getPassengers();
    @Nullable MinecraftEntity getControllingPassenger();

//    @Nullable Pair<MinecraftEntity, Vector3d> getTargetedEntity(Double distance);
//    @Nullable MinecraftEntity getNearestEntity(String type, Double radius);

    String getName();
    // Return the identifier for this kind of entity.
    // Used as a CEM key (TODO: Add more information to the CEM key, so different pigs can have different avatars, for example?)
    MinecraftIdentifier getType();
//    String getDimensionName();
    EntityPose getPose();

    int getPermissionLevel();
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

}