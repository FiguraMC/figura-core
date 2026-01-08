package org.figuramc.figura_core.script_languages.lua.type_apis.world;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDynamicField;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.world.MinecraftWorld;
import org.figuramc.figura_core.script_hooks.callback.items.BlockStateView;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.WorldView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

@LuaTypeAPI(typeName = "World", wrappedClass = WorldView.class)
public class WorldViewAPI {

    public static LuaUserdata wrap(WorldView<?> worldView, LuaRuntime state) {
        return new LuaUserdata(worldView, state.figuraMetatables.worldView);
    }

    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable entities(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return uuidTable(s, self, fetchWorld(s, self).getEntities()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable players(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return uuidTable(s, self, fetchWorld(s, self).getPlayers()); }
    @LuaExpose @LuaPassState public static BlockStateView<?> blockAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return new BlockStateView<>(fetchWorld(s, self).getBlockState(x, y, z), self); }
    @LuaExpose @LuaPassState public static BlockStateView<?> blockAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return blockAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }

    @LuaExpose @LuaPassState @LuaDynamicField public static String dimension(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).getDimension().toString(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static int height(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).getHeight(); }

    @LuaExpose @LuaPassState public static float redstoneAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).getRedstonePower(x, y, z); }
    @LuaExpose @LuaPassState public static float redstoneAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return redstoneAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }
    @LuaExpose @LuaPassState public static float strongRedstoneAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).getStrongRedstonePower(x, y, z); }
    @LuaExpose @LuaPassState public static float strongRedstoneAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return strongRedstoneAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }

    @LuaExpose @LuaPassState public static float lightAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).getLight(x, y, z); }
    @LuaExpose @LuaPassState public static float lightAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return lightAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }
    @LuaExpose @LuaPassState public static float skyLightAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).getSkyLight(x, y, z); }
    @LuaExpose @LuaPassState public static float skyLightAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return skyLightAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }
    @LuaExpose @LuaPassState public static float blockLightAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).getBlockLight(x, y, z); }
    @LuaExpose @LuaPassState public static float blockLightAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return blockLightAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }

    @LuaExpose @LuaPassState @LuaDynamicField public static long time(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).getTime(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static long dayTime(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).getTimeOfDay(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static long day(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).getDay(); }

    @LuaExpose @LuaPassState public static float rainGradient(LuaRuntime s, WorldView<?> self, float tickDelta) throws LuaError, LuaOOM { return fetchWorld(s, self).getRainGradient(tickDelta); }
    @LuaExpose @LuaPassState public static float rainGradient(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return rainGradient(s, self, 1.0f); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean isThundering(LuaRuntime s, WorldView<?> self) throws LuaError, LuaOOM { return fetchWorld(s, self).isThundering(); }
    @LuaExpose @LuaPassState public static boolean isOpenSkyAt(LuaRuntime s, WorldView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return fetchWorld(s, self).isOpenSky(x, y, z); }
    @LuaExpose @LuaPassState public static boolean isOpenSkyAt(LuaRuntime s, WorldView<?> self, Vector3d pos) throws LuaError, LuaOOM { return isOpenSkyAt(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }

    private static @NotNull MinecraftWorld fetchWorld(LuaState state, WorldView<?> worldView) throws LuaError, LuaOOM {
        // Get world
        MinecraftWorld world = worldView.getValue();
        if (world == null) throw new LuaError("Attempt to use world view after it was revoked!", state.allocationTracker);
        return world;
    }

    // Helpers
    private static LuaTable uuidTable(LuaRuntime s, WorldView<?> self, Iterable<MinecraftEntity> entities) throws LuaError, LuaOOM {
        LuaTable tab = new LuaTable(s.allocationTracker);
        for (var entity : entities)
            tab.rawset(entity.getUUID().toString(), EntityViewAPI.wrap(new EntityView<>(entity, self), s));
        return tab;
    }

}
