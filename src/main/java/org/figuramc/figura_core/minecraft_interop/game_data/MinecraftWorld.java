package org.figuramc.figura_core.minecraft_interop.game_data;

import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftPlayer;
import org.figuramc.figura_core.util.functional.BiThrowingConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


/**
 * A view into the current Minecraft World
 */
public interface MinecraftWorld {

    // BiomeAPI getBiome(Double x, Double y, Double z)

    // TODO: Consider a way to get all Map IDs?
    @Nullable MapData getMapData(int id);
    record MapData(int centerX, int centerZ, boolean locked, int scale) {}

    <E1 extends Throwable, E2 extends Throwable> void forEachPlayer(BiThrowingConsumer<MinecraftPlayer, E1, E2> consumer) throws E1, E2;
    <E1 extends Throwable, E2 extends Throwable> void forEachEntity(BiThrowingConsumer<MinecraftEntity, E1, E2> consumer) throws E1, E2;
    MinecraftBlockState getBlockState(int x, int y, int z);
    @Nullable MinecraftEntity getEntity(UUID uuid);

    // Name of the dimension including prefix, like "minecraft:overworld"
    MinecraftIdentifier getDimension();

    int getRedstonePower(int x, int y, int z);
    int getStrongRedstonePower(int x, int y, int z);
    int getMoonPhase();
    int getLightLevel(int x, int y, int z);
    int getSkyLightLevel(int x, int y, int z);
    int getBlockLightLevel(int x, int y, int z);
    int getHeight();

    long getTime();
    long getTimeOfDay();
    long getDay();
    float getRainGradient(float delta);

    boolean isChunkLoaded(int x, int y, int z);
    boolean isThundering();
    boolean isOpenSky(int x, int y, int z);
}
