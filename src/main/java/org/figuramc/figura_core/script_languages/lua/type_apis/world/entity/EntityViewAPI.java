package org.figuramc.figura_core.script_languages.lua.type_apis.world.entity;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDynamicField;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityPose;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.EquipmentSlot;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.minecraft_interop.game_data.types.AABB;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.ItemStackView;
import org.figuramc.figura_core.script_hooks.callback.items.WorldView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec3API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;

@LuaTypeAPI(typeName = "Entity", wrappedClass = EntityView.class)
public class EntityViewAPI {

    public static LuaUserdata wrap(EntityView<?> entityView, LuaRuntime state) {
        return new LuaUserdata(entityView, state.figuraMetatables.entityView);
    }

    @LuaExpose @LuaPassState @LuaDynamicField public static WorldView<?> world(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return new WorldView<>(fetchEntity(s, self).getWorld(), self); }

    @LuaExpose @LuaPassState @LuaDynamicField public static String name(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getName(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static String uuid(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getUUID().toString(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static String type(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getType().toString(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isPlayer(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isPlayer(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isLivingEntity(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isLivingEntity(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean hasInventory(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).hasInventory(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static Vector3d pos(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getPosition(1.0f, new Vector3d()); }
    @LuaExpose @LuaPassState public static Vector3d getPos(LuaRuntime s, EntityView<?> self, float tickDelta) throws LuaError, LuaOOM { return fetchEntity(s, self).getPosition(tickDelta, new Vector3d()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static Vector2d rot(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return new Vector2d(fetchEntity(s, self).getRotation(1.0f, new Vector2f())); }
    @LuaExpose @LuaPassState public static Vector2d getRot(LuaRuntime s, EntityView<?> self, float tickDelta) throws LuaError, LuaOOM { return new Vector2d(fetchEntity(s, self).getRotation(tickDelta, new Vector2f())); }
    @LuaExpose @LuaPassState @LuaDynamicField public static Vector3d dir(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getLookDirection(1.0f, new Vector3d()); }
    @LuaExpose @LuaPassState public static Vector3d getDir(LuaRuntime s, EntityView<?> self, float tickDelta) throws LuaError, LuaOOM { return fetchEntity(s, self).getLookDirection(tickDelta, new Vector3d()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static Vector3d vel(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getVelocity(new Vector3d()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static double eyeHeight(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getEyeHeight(); }

    // Returns table of min, max vector
    // TODO: should this go back to 0.1.0 days and be just the width/height of the bounding box? or is the real bounding box more helpful?
    // TODO: consider actual api for bounding boxes instead of pair of vec3s? probably too niche though
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable boundingBox(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM {
        AABB aabb = fetchEntity(s, self).getBoundingBox();
        return ValueFactory.tableOf(
                s.allocationTracker,
                Vec3API.wrap(new Vector3d(aabb.x1(), aabb.y1(), aabb.z1()), s),
                Vec3API.wrap(new Vector3d(aabb.x2(), aabb.y2(), aabb.z2()), s)
        );
    }

    // TODO rename these "level/ticks/max ticks" things since it feels cursed and inconsistent and bad,,
    @LuaExpose @LuaPassState @LuaDynamicField public static float freezeProgress(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getFreezeTime() / e.getFreezeDuration(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float freezeTime(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getFreezeTime(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float freezeDuration(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getFreezeDuration(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float airLevel(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getAir() / e.getMaxAir(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float air(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getAir(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float maxAir(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getMaxAir(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float healthLevel(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getHealth() / e.getMaxHealth(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float health(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getHealth(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float maxHealth(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getMaxHealth(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float armor(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getArmor(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float absorption(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getAbsorption(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float dyingProgress(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getDyingTime() / e.getDyingDuration(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float dyingTime(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getDyingTime(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float dyingDuration(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self); return e.getDyingDuration(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean glowing(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isGlowing(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean invisible(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isInvisible(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean silent(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSilent(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean onFire(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isOnFire(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int arrowCount(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getArrowCount(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int stingerCount(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getStingerCount(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static EntityPose pose(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getPose(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean sneaking(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSneaking(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean sprinting(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSprinting(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean moving(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isMoving(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean movingHorizontally(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isMovingHorizontally(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isOnGround(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isOnGround(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean falling(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isFalling(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean climbing(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isClimbing(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean gliding(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isGliding(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean blocking(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isBlocking(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean swimming(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSwimming(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean crawling(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isCrawling(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean riptideSpinning(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isRiptideSpinning(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean leftHanded(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isLeftHanded(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean swinging(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSwinging(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float swingTime(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getSwingTime(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float swingDuration(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getSwingDuration(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean swingingOffHand(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).swingingOffHand(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean slim(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isSlim(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean inWater(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isInWater(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean underwater(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isUnderwater(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean inLava(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isInLava(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean inRain(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isInRain(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable EntityView<?> vehicle(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self).getVehicle(); return e == null ? null : new EntityView<>(e, self); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable EntityView<?> controlledVehicle(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self).getControlledVehicle(); return e == null ? null : new EntityView<>(e, self); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable passengers(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return s.listToTable(fetchEntity(s, self).getPassengers(), (s2, e) -> EntityViewAPI.wrap(new EntityView<>(e, self), s2)); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable EntityView<?> controllingPassenger(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftEntity e = fetchEntity(s, self).getControllingPassenger(); return e == null ? null : new EntityView<>(e, self); }

    @LuaExpose @LuaPassState public static @Nullable ItemStackView<?> getItem(LuaRuntime s, EntityView<?> self, EquipmentSlot slot) throws LuaError, LuaOOM { MinecraftItemStack item = fetchEntity(s, self).getItem(slot); return item == null ? null : new ItemStackView<>(item, self); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> mainItem(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.MAINHAND); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> offHandItem(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.OFFHAND); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> helmet(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.HEAD); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> chestplate(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.CHEST); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> leggings(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.LEGS); }
    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> boots(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return getItem(s, self, EquipmentSlot.FEET); }

    @LuaExpose @LuaPassState @LuaDynamicField public static @Nullable ItemStackView<?> activeItem(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { MinecraftItemStack item = fetchEntity(s, self).getActiveItem(); return item == null ? null : new ItemStackView<>(item, self); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int itemUseTicks(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).getItemUseTicks(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean fishing(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM { return fetchEntity(s, self).isFishing(); }

    // Helper to fetch entity, or error if revoked
    private static @NotNull MinecraftEntity fetchEntity(LuaState state, EntityView<?> entityView) throws LuaError, LuaOOM {
        // Get entity
        @Nullable MinecraftEntity entity = entityView.getValue();
        // If null (aka revoked), error
        if (entity == null) throw new LuaError("Attempt to use entity view after it was revoked!", state.allocationTracker);
        // Return the non-null entity
        return entity;
    }

}
