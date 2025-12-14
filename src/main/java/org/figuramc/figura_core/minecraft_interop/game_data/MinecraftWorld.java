package org.figuramc.figura_core.minecraft_interop.game_data;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.script_languages.lua.type_apis.world.entity.EntityViewAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * A view into the current MinecraftWorld
 */
public interface MinecraftWorld {

    // BiomeAPI getBiome(Double x, Double y, Double z)

    Map<String, MinecraftEntity> getPlayers();
    //Map<String, LuaTable> avatarVars();
    List<MinecraftEntity> getEntities(int x1, int y1, int z1, int x2, int y2, int z2);
    @Nullable
    HashMap<String, Object> getMapData(int id);
    List<MinecraftBlockState> getBlocks(int x, int y, int z, int w, int t, int h);

    MinecraftBlockState getBlockState(int x, int y, int z);
    MinecraftBlockState newBlock(String string, int x, int y, int z);
    MinecraftItemStack newItem(String string, int count, int damage);
    @Nullable
    MinecraftEntity getEntity(UUID uuid);

    String getCurrentDimension();

    int getRedstonePower(int x, int y, int z);
    int getStrongRedstonePower(int x, int y, int z);
    int getMoonPhase();
    int getLightLevel(int x, int y, int z);
    int getSkyLightLevel(int x, int y, int z);
    int getBlockLightLevel(int x, int y, int z);
    int getHeight();

    double getTime(double delta);
    double getTimeOfDay(double delta);
    double getDayTime(double delta);
    double getDay(double delta);
    double getRainGradient(Float delta);

    boolean isChunkLoaded(int x, int y, int z);
    boolean isThundering(int x, int y, int z);
    boolean isOpenSky(int x, int y, int z);
}
