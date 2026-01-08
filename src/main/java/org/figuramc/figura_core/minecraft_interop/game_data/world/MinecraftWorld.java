package org.figuramc.figura_core.minecraft_interop.game_data.world;

import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A view into the current Minecraft World
 */
public interface MinecraftWorld {

    List<MinecraftEntity> getEntities();
    List<MinecraftEntity> getPlayers();
    MinecraftBlockState getBlockState(int x, int y, int z);
    @Nullable MinecraftEntity getEntity(UUID uuid);

    MinecraftIdentifier getDimension();
    int getHeight();

    float getRedstonePower(int x, int y, int z); // Like if a block is powered by redstone dust
    float getStrongRedstonePower(int x, int y, int z); // Like if a block is powered by a repeater

    float getLight(int x, int y, int z);
    float getSkyLight(int x, int y, int z);
    float getBlockLight(int x, int y, int z);

    long getTime(); // Ticks elapsed, constantly incrementing even if day-night cycle is frozen
    long getTimeOfDay(); // Ticks since the start of the day in the day-night cycle
    long getDay(); // The number of days that have passed in the day-night cycle
    int getMoonPhase(); // TODO: EnumLike for this? Most people only care about full moon, an enum would make it easier than knowing arbitrary numbers and future-proofs it if Minecraft adds more moon phases

    float getRainGradient(float tickDelta); // Rain amount, interpolated
    boolean isThundering();
    boolean isOpenSky(int x, int y, int z);

}
