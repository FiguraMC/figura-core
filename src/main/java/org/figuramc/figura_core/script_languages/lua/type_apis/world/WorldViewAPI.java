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
        // Get the player hashmap
        Map<String, MinecraftEntity> player_map = fetchWorld(s, self).getPlayers();
        LuaTable table = new LuaTable(player_map.size(), 1, s.allocationTracker);
        for (Map.Entry<String, MinecraftEntity> entry : player_map.entrySet()) {
            // Convert each MinecraftEntity into an EntityViewAPI
            table.rawset(entry.getKey(), EntityViewAPI.wrap(new EntityView<>(entry.getValue()), s));
        }
        return table;
    }

    @LuaExpose @LuaPassState
    public static LuaTable entities(LuaRuntime s, WorldView<?> self, int x1, int y1, int z1, int x2, int y2, int z2) throws LuaError, LuaUncatchableError {
        // Get the entities present
        List<MinecraftEntity> entities = fetchWorld(s, self).getEntities(x1, y1, z1, x2, y2, z2);
        LuaTable table = new LuaTable(entities.size(), 1, s.allocationTracker);
        int i = 1;
        for (MinecraftEntity entity : entities) {
            // Convert each MinecraftEntity into an EntityViewAPI
            table.rawset(i, EntityViewAPI.wrap(new EntityView<>(entity), s));
            i++;
        }
        return table;
    }
    @LuaExpose @LuaPassState
    public static LuaTable entities(LuaRuntime s, WorldView<?> self, Vector3d pos1, Vector3d pos2) throws LuaError, LuaUncatchableError {
        return entities(s, self, (int) pos1.x, (int) pos1.y, (int) pos1.z, (int) pos2.x, (int) pos2.y, (int) pos2.z);
    }

    @LuaExpose @LuaPassState
    public static LuaTable getMapData(LuaRuntime s, WorldView<?> self, int id) throws LuaError, LuaUncatchableError {
        HashMap<String, Object> map_data = fetchWorld(s, self).getMapData(id);
        if (map_data == null)
            return null;

        // Just pass one table to the other
        LuaTable table = new LuaTable(map_data.size(), 1, s.allocationTracker);

        table.rawset("center_x", LuaInteger.valueOf((int) map_data.get("center_x")));
        table.rawset("center_z", LuaInteger.valueOf((int) map_data.get("center_z")));
        table.rawset("locked", LuaBoolean.valueOf((boolean) map_data.get("locked")));
        table.rawset("scale", LuaInteger.valueOf((int) map_data.get("scale")));

        return table;
    }

    @LuaExpose @LuaPassState
    public static LuaTable blocks(LuaRuntime s, WorldView<?> self, int x, int y, int z, int w, int t, int h) throws LuaError, LuaUncatchableError {
        List<MinecraftBlockState> blocks = fetchWorld(s, self).getBlocks(x, y, z, w, t, h);
        LuaTable table = new LuaTable(blocks.size(), 1, s.allocationTracker);
        int i = 1;
        for (MinecraftBlockState block : blocks) {
            table.rawset(i, BlockStateViewAPI.wrap(new BlockStateView<>(block), s));
            i++;
        }
        return table;
    }
    @LuaExpose @LuaPassState
    public static LuaTable blocks(LuaRuntime s, WorldView<?> self, Vector3d pos1, Vector3d pos2) throws LuaError, LuaUncatchableError {
        return blocks(s, self, (int) pos1.x, (int) pos1.y, (int) pos1.z, (int) pos2.x, (int) pos2.y, (int) pos2.z);
    }

    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> blockState(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return new BlockStateView<>(fetchWorld(s, self).getBlockState(x, y, z));
    }
    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> blockState(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return new BlockStateView<>(fetchWorld(s, self).getBlockState((int) pos.x, (int) pos.y, (int) pos.z));
    }

    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> newBlock(LuaState s, WorldView<?> self, String data, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return new BlockStateView<>(fetchWorld(s, self).newBlock(data, x, y, z));
    }
    @LuaExpose @LuaPassState
    public static BlockStateView<MinecraftBlockState> newBlock(LuaState s, WorldView<?> self, String data, Vector3d pos) throws LuaError, LuaUncatchableError {
        return new BlockStateView<>(fetchWorld(s, self).newBlock(data, (int) pos.x, (int) pos.y, (int) pos.z));
    }

    /* TODO: ItemStackAPI must exist before implementation
    @LuaExpose @LuaPassState
    public static <MinecraftBlockState> newItem(LuaState s, WorldView<?> self, String data, int count, int damage) throws LuaError, LuaUncatchableError {
        return new <>(fetchWorld(s, self).newItem(data, count, damage));
    }*/

    @LuaExpose @LuaPassState
    public static EntityView<MinecraftEntity> entity(LuaState s, WorldView<?> self, UUID uuid) throws LuaError, LuaUncatchableError {
        MinecraftEntity entity = fetchWorld(s, self).getEntity(uuid);
        if (entity == null)
            return null;
        return new EntityView<>(entity);
    }

    @LuaExpose @LuaPassState
    public static String dimension(LuaState s, WorldView<?> self) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getCurrentDimension();
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
    public static double time(LuaState s, WorldView<?> self, double delta) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getTime(delta);
    }

    @LuaExpose @LuaPassState
    public static double timeOfDay(LuaState s, WorldView<?> self, double delta) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getTimeOfDay(delta);
    }

    @LuaExpose @LuaPassState
    public static double dayTime(LuaState s, WorldView<?> self, double delta) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getDayTime(delta);
    }

    @LuaExpose @LuaPassState
    public static double day(LuaState s, WorldView<?> self, double delta) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).getDay(delta);
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
    public static boolean isThundering(LuaState s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaUncatchableError {
        return fetchWorld(s, self).isThundering(x, y, z);
    }
    @LuaExpose @LuaPassState
    public static boolean isThundering(LuaState s, WorldView<?> self, Vector3d pos) throws LuaError, LuaUncatchableError {
        return isThundering(s, self, (int) pos.x, (int) pos.y, (int) pos.z);
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
        MinecraftWorld world = worldView.getWorld();

        if (world == null) throw new LuaError("Attempt to use world view after it was revoked!", state.allocationTracker);

        return world;
    }
}
