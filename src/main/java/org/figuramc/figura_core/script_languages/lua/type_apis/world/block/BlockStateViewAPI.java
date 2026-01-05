package org.figuramc.figura_core.script_languages.lua.type_apis.world.block;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.script_hooks.callback.items.BlockStateView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec3API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;

@LuaTypeAPI(typeName = "BlockState", wrappedClass = BlockStateView.class)
public class BlockStateViewAPI {

    public static LuaUserdata wrap(BlockStateView<?> blockStateView, LuaRuntime state) {
        return new LuaUserdata(blockStateView, state.figuraMetatables.blockStateView);
    }

    @LuaExpose @LuaPassState
    public static String id(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getIdentifier().toString();
    }

    @LuaExpose @LuaPassState
    public static Vector3d pos(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return new Vector3d(fetchBlockState(s, self).getBundledPos(new Vector3i()));
    }

    @LuaExpose @LuaPassState
    public static BlockStateView<?> at(LuaRuntime s, BlockStateView<?> self, Vector3d newPos) throws LuaError, LuaOOM {
        return new BlockStateView<>(fetchBlockState(s, self).withBundledPos(new Vector3i(newPos, RoundingMode.TRUNCATE)), self);
    }

    @LuaExpose @LuaPassState
    public static LuaTable collisionShape(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return processVoxelShape(s, fetchBlockState(s, self).getCollisionShape());
    }

    @LuaExpose @LuaPassState
    public static LuaTable outlineShape(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return processVoxelShape(s, fetchBlockState(s, self).getOutlineShape());
    }

//    @LuaExpose @LuaPassState
//    public static LuaTable sounds(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
//        Map<String, Object> sounds = fetchBlockState(s, self).getSounds();
//        LuaTable table = new LuaTable(sounds.size(), 1, s.allocationTracker);
//
//        // Pass the values from the map object
//        table.rawset("pitch", LuaDouble.valueOf((double) sounds.get("pitch")));
//        table.rawset("volume", LuaDouble.valueOf((double) sounds.get("volume")));
//        table.rawset("break", LuaString.valueOf(s.allocationTracker, (String) sounds.get("break")));
//        table.rawset("fall", LuaString.valueOf(s.allocationTracker, (String) sounds.get("fall")));
//        table.rawset("hit", LuaString.valueOf(s.allocationTracker, (String) sounds.get("hit")));
//        table.rawset("place", LuaString.valueOf(s.allocationTracker, (String) sounds.get("place")));
//        table.rawset("step", LuaString.valueOf(s.allocationTracker, (String) sounds.get("step")));
//
//        return table;
//    }

    @LuaExpose @LuaPassState
    public static LuaTable properties(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return s.stringListToTable(fetchBlockState(s, self).getProperties());
    }

    @LuaExpose @LuaPassState
    public static LuaTable tags(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return s.listToTable(fetchBlockState(s, self).getTags(), (r, ident) -> LuaString.valueOf(r.allocationTracker, ident.toString()));
    }

    @LuaExpose @LuaPassState
    public static LuaTable fluidTags(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return s.listToTable(fetchBlockState(s, self).getFluidTags(), (r, ident) -> LuaString.valueOf(r.allocationTracker, ident.toString()));
    }

    // TODO: getEntityData

    @LuaExpose @LuaPassState
    public static Vector3d mapColor(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return new Vector3d(fetchBlockState(s, self).getMapColor(new Vector3f()));
    }

    /* TODO: MinecraftItemAPI
    @LuaExpose @LuaPassState
    public static MinecraftItemAPI asItem(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getMapColor();
    }*/

    // TODO NBT discussion
    @LuaExpose @LuaPassState
    public static String stateString(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).toStateString();
    }

    @LuaExpose @LuaPassState
    public static int opacity(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getOpacity();
    }

    @LuaExpose @LuaPassState
    public static int comparatorOutput(LuaRuntime s, BlockStateView<?> self, String direction) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getComparatorOutput(direction);
    }

    @LuaExpose @LuaPassState
    public static int luminance(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getLuminance();
    }

    @LuaExpose @LuaPassState
    public static float hardness(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getHardness();
    }

    @LuaExpose @LuaPassState
    public static float friction(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getFriction();
    }

    @LuaExpose @LuaPassState
    public static float velocityModifier(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getVelocityModifier();
    }

    @LuaExpose @LuaPassState
    public static float jumpVelicityModifier(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getJumpVelocityModifier();
    }

    @LuaExpose @LuaPassState
    public static float blastResistance(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).getBlastResistance();
    }

    @LuaExpose @LuaPassState
    public static boolean isTranslucent(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).isTranslucent();
    }

    @LuaExpose @LuaPassState
    public static boolean isSolidBlock(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).isSolidBlock();
    }

    @LuaExpose @LuaPassState
    public static boolean isFullCube(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).isFullCube();
    }

    @LuaExpose @LuaPassState
    public static boolean hasEmissiveLighting(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).hasEmissiveLighting();
    }

    @LuaExpose @LuaPassState
    public static boolean hasBlockEntity(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).hasBlockEntity();
    }

    @LuaExpose @LuaPassState
    public static boolean isOpaque(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).isOpaque();
    }

    @LuaExpose @LuaPassState
    public static boolean emitsRedstonePower(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).emitsRedstonePower();
    }

    @LuaExpose @LuaPassState
    public static boolean hasCollision(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).hasCollision();
    }

    @LuaExpose @LuaPassState
    public static boolean isAir(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM {
        return fetchBlockState(s, self).isAir();
    }

    // Helper to translate the List<AABB> into a Table
    private static LuaTable processVoxelShape(LuaRuntime s, List<MinecraftBlockState.AABB> shape) throws LuaOOM {
        return s.listToTable(shape, (r, cube) -> {
            LuaTable cubeTable = new LuaTable(2, 0, r.allocationTracker);
            cubeTable.rawset(1, Vec3API.wrap(new Vector3d(cube.x1(), cube.y1(), cube.z1()), r));
            cubeTable.rawset(2, Vec3API.wrap(new Vector3d(cube.x2(), cube.y2(), cube.z2()), r));
            return cubeTable;
        });
    }

    // Helper to get the MinecraftBlockState instance
    private static @NotNull MinecraftBlockState fetchBlockState(LuaState state, BlockStateView<?> blockStateView) throws LuaError, LuaOOM {
        @Nullable MinecraftBlockState blockState = blockStateView.getValue();
        if (blockState == null) throw new LuaError("Attempt to use a BlockStateView after it was revoked!", state.allocationTracker);
        // BlockState is still valid.
        return blockState;
    }
}
