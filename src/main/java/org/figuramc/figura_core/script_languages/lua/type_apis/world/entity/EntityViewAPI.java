package org.figuramc.figura_core.script_languages.lua.type_apis.world.entity;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;

import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec3API;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.List;

@LuaTypeAPI(typeName = "Entity", wrappedClass = EntityView.class)
public class EntityViewAPI {

    public static LuaUserdata wrap(EntityView<?> entityView, LuaRuntime state) {
        return new LuaUserdata(entityView, state.figuraMetatables.entityView);
    }

    @LuaExpose @LuaPassState
    public static Vector3d pos(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return pos(s, self, 1.0f);
    }
    @LuaExpose @LuaPassState
    public static Vector3d pos(LuaState s, EntityView<?> self, float delta) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getPosition(delta, new Vector3d());
    }
    @LuaExpose @LuaPassState
    public static Vector2d rot(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return rot(s, self, 1.0f);
    }
    @LuaExpose @LuaPassState
    public static Vector2d rot(LuaState s, EntityView<?> self, float delta) throws LuaError, LuaOOM {
        return new Vector2d(fetchEntity(s, self).getRotation(delta, new Vector2f()));
    }
    @LuaExpose @LuaPassState
    public static Vector3d vel(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getVelocity(new Vector3d());
    }
    @LuaExpose @LuaPassState
    public static Vector3d lookDir(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return lookDir(s, self, 1.0f);
    }
    @LuaExpose @LuaPassState
    public static Vector3d lookDir(LuaState s, EntityView<?> self, float delta) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getLookDirection(delta, new Vector3d());
    }

    @LuaExpose @LuaPassState
    public static EntityView<MinecraftEntity> vehicle(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        MinecraftEntity vehicle = fetchEntity(s, self).getVehicle();
        return vehicle == null ? null : new EntityView<>(vehicle, self);
    }

    @LuaExpose @LuaPassState
    public static EntityView<MinecraftEntity> controlledVehicle(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        MinecraftEntity vehicle = fetchEntity(s, self).getControlledVehicle();
        return vehicle == null ? null : new EntityView<>(vehicle, self);
    }


    @LuaExpose @LuaPassState
    public static LuaTable passengers(LuaRuntime s, EntityView<?> self) throws LuaError, LuaOOM {
        // Get the passengers list and convert
        return s.listToTable(
                fetchEntity(s, self).getPassengers(),
                (r, passenger) -> EntityViewAPI.wrap(new EntityView<>(passenger, self), r)
        );
    }

    @LuaExpose @LuaPassState
    public static EntityView<MinecraftEntity> controllingPassenger(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        MinecraftEntity passenger = fetchEntity(s, self).getControllingPassenger();
        return passenger == null ? null : new EntityView<>(passenger, self);
    }

//    @LuaExpose @LuaPassState
//    public static Varargs targetedEntity(LuaRuntime s, EntityView<?> self, Double distance) throws LuaError, LuaOOM {
//      Pair<MinecraftEntity, Vector3d> pair = fetchEntity(s, self).getTargetedEntity(distance);
//        if (pair == null)
//            return null;
//
//        return ValueFactory.varargsOf(EntityViewAPI.wrap(new EntityView<>(pair.a()), s), Vec3API.wrap(pair.b(), s));
//    }
//
//    @LuaExpose @LuaPassState
//    public static EntityView<MinecraftEntity> nearestEntity(LuaState s, EntityView<?> self, String type, Double radius) throws LuaError, LuaOOM {
//        MinecraftEntity nearest = fetchEntity(s, self).getNearestEntity(type, radius);
//        if (nearest != null) {
//            return new EntityView<>(nearest);
//        }
//        return null;
//    }


    @LuaExpose @LuaPassState
    public static String name(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getName();
    }

    @LuaExpose @LuaPassState
    public static String type(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getType().toString();
    }

    @LuaExpose @LuaPassState
    public static String getPose(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getPose().name;
    }

    @LuaExpose @LuaPassState
    public static int permissionLevel(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getPermissionLevel();
    }

    @LuaExpose @LuaPassState
    public static int frozenTicks(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getFrozenTicks();
    }

    @LuaExpose @LuaPassState
    public static int maxAir(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getMaxAir();
    }

    @LuaExpose @LuaPassState
    public static float eyeHeight(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getEyeHeight();
    }

    @LuaExpose @LuaPassState
    public static boolean isPlayer(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isPlayer();
    }

    @LuaExpose @LuaPassState
    public static boolean isCrouching(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isCrouching();
    }

    @LuaExpose @LuaPassState
    public static boolean isSprinting(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isSprinting();
    }

    @LuaExpose @LuaPassState
    public static boolean isMoving(LuaState s, EntityView<?> self, boolean ignoreY) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isMoving(ignoreY);
    }

    @LuaExpose @LuaPassState
    public static boolean isOnGround(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isOnGround();
    }

    @LuaExpose @LuaPassState
    public static boolean isFalling(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isFalling();
    }

    @LuaExpose @LuaPassState
    public static boolean isWet(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isWet();
    }

    @LuaExpose @LuaPassState
    public static boolean isInWater(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isInWater();
    }

    @LuaExpose @LuaPassState
    public static boolean isInLava(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isInLava();
    }

    @LuaExpose @LuaPassState
    public static boolean isInRain(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isInRain();
    }

    @LuaExpose @LuaPassState
    public static boolean isUnderWater(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isUnderWater();
    }

    @LuaExpose @LuaPassState
    public static boolean isGlowing(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isGlowing();
    }

    @LuaExpose @LuaPassState
    public static boolean isInvisible(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isInvisible();
    }

    @LuaExpose @LuaPassState
    public static boolean isSilent(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isSilent();
    }

    @LuaExpose @LuaPassState
    public static boolean isOnFire(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isOnFire();
    }

    @LuaExpose @LuaPassState
    public static boolean isAlive(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).isAlive();
    }

    @LuaExpose @LuaPassState
    public static boolean hasInventory(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).hasInventory();
    }

    /*
    @LuaExpose @LuaPassState
    public static Object nbt(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getNBT();
    }*/

    // TODO: getTargetedBlock

    @LuaExpose @LuaPassState
    public static boolean hasAvatar(LuaState s, EntityView<?> self) throws LuaError, LuaOOM {
        MinecraftEntity e = fetchEntity(s, self);
        return AvatarManagers.ENTITIES.get(e.getUUID()) != null;
    }

    /* TODO
    @LuaExpose @LuaPassState
    public static Object getVariable(LuaState s, EntityView<?> self, String key) throws LuaError, LuaOOM {
        return fetchEntity(s, self).getVariable(key);
    }
     */


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
