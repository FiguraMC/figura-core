package org.figuramc.figura_core.script_languages.lua.type_apis.world;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftWorld;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.script_hooks.callback.items.BlockStateView;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.WorldView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.block.BlockStateViewAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LuaTypeAPI(typeName = "World", wrappedClass = WorldView.class)
public class WorldViewAPI {

    public static LuaUserdata wrap(WorldView<?> worldView, LuaRuntime state) {
        return new LuaUserdata(worldView, state.figuraMetatables.worldView);
    }

    @LuaExpose @LuaPassState
    public static LuaTable players(LuaRuntime s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        LuaTable table = new LuaTable(s.allocationTracker);
        fetchWorld(s, self).forEachPlayer(player -> table.rawset(
                player.getName(),
                EntityViewAPI.wrap(new EntityView<>(player, self), s)
        ));
        return table;
    }

    @LuaExpose @LuaPassState
    public static LuaTable entities(LuaRuntime s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        LuaTable table = new LuaTable(s.allocationTracker);
        int[] i = new int[1];
        fetchWorld(s, self).forEachEntity(entity -> table.rawset(
                ++i[0],
                EntityViewAPI.wrap(new EntityView<>(entity, self), s))
        );
        return table;
    }

    // Return nil if this ID has no data associated, or return a table with various key/value pairs
    @LuaExpose @LuaPassState
    public static LuaValue getMapData(LuaRuntime s, WorldView<?> self, int id) throws LuaError, LuaUncatchableError {
        MinecraftWorld.MapData mapData = fetchWorld(s, self).getMapData(id);
        if (mapData == null) return Constants.NIL;

        LuaTable table = new LuaTable(s.allocationTracker);
        table.rawset("center_x", LuaInteger.valueOf(mapData.centerX()));
        table.rawset("center_z", LuaInteger.valueOf(mapData.centerZ()));
        table.rawset("locked", LuaBoolean.valueOf(mapData.locked()));
        table.rawset("scale", LuaInteger.valueOf(mapData.scale()));

        return table;
    }

    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> blockState(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return new BlockStateView<>(fetchWorld(s, self).getBlockState(x, y, z), self);
    }
    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> blockState(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return blockState(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    /* TODO: ItemStackAPI must exist before implementation
    @LuaExpose @LuaPassState
    public static <MinecraftBlockState> newItem(LuaState s, WorldView<?> self, String data, int count, int damage) throws LuaError, LuaUncatchableError {
        return new <>(fetchWorld(s, self).newItem(data, count, damage));
    }*/

    @LuaExpose @LuaPassState
    public static EntityView<MinecraftEntity> entity(LuaState s, WorldView<?> self, UUID uuid) throws LuaError, LuaUncatchableError {
        MinecraftEntity entity = fetchWorld(s, self).getEntity(uuid);
        return entity == null ? null : new EntityView<>(entity, self);
    }

    @LuaExpose @LuaPassState
    public static String dimension(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getDimension().toString();
    }

    @LuaExpose @LuaPassState
    public static int redstonePower(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getRedstonePower(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static int redstonePower(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return redstonePower(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static int strongRedstonePower(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getStrongRedstonePower(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static int strongRedstonePower(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return strongRedstonePower(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static int moonPhase(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getMoonPhase();
    }

    @LuaExpose @LuaPassState
    public static int lightLevel(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getLightLevel(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static int lightLevel(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return lightLevel(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static int skyLightLevel(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getSkyLightLevel(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static int skyLightLevel(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return skyLightLevel(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static int blockLightLevel(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getBlockLightLevel(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static int blockLightLevel(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return blockLightLevel(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static int height(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getHeight();
    }

    @LuaExpose @LuaPassState
    public static long time(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getTime();
    }

    @LuaExpose @LuaPassState
    public static double timeOfDay(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getTimeOfDay();
    }

    @LuaExpose @LuaPassState
    public static double day(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getDay();
    }

    @LuaExpose @LuaPassState
    public static double rainGradient(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return rainGradient(s, self, 1.0f);
    }
    @LuaExpose @LuaPassState
    public static double rainGradient(LuaState s, WorldView<?> self, float delta) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getRainGradient(delta);
    }

    @LuaExpose @LuaPassState
    public static boolean isChunkLoaded(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).isChunkLoaded(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static boolean isChunkLoaded(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return isChunkLoaded(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    @LuaExpose @LuaPassState
    public static boolean isThundering(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).isThundering();
    }

    @LuaExpose @LuaPassState
    public static boolean isOpenSky(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).isOpenSky(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static boolean isOpenSky(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return isOpenSky(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
    }

    private static @NotNull MinecraftWorld fetchWorld(LuaState state, WorldView<?> worldView) throws LuaError, LuaUncatchableError {
        // Get world
        MinecraftWorld world = worldView.getValue();
        if (world == null) throw new LuaError("Attempt to use world view after it was revoked!", state.allocationTracker);
        return world;
    }
}
