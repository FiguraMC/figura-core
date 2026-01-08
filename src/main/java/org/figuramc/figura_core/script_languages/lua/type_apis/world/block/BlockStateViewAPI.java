package org.figuramc.figura_core.script_languages.lua.type_apis.world.block;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDynamicField;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.types.AABB;
import org.figuramc.figura_core.script_hooks.callback.items.BlockStateView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec3API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.List;

@LuaTypeAPI(typeName = "BlockState", wrappedClass = BlockStateView.class)
public class BlockStateViewAPI {

    public static LuaUserdata wrap(BlockStateView<?> blockStateView, LuaRuntime state) {
        return new LuaUserdata(blockStateView, state.figuraMetatables.blockStateView);
    }

    // Get with different pos
    @LuaExpose @LuaPassState public static BlockStateView<?> withPos(LuaRuntime s, BlockStateView<?> self, int x, int y, int z) throws LuaError, LuaOOM { return new BlockStateView<>(fetchBlockState(s, self).withPos(x, y, z), self); }
    @LuaExpose @LuaPassState public static BlockStateView<?> withPos(LuaRuntime s, BlockStateView<?> self, Vector3d pos) throws LuaError, LuaOOM { return withPos(s, self, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z)); }

    @LuaExpose @LuaPassState @LuaDynamicField public static String id(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getId().toString(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static Vector3d pos(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return new Vector3d(fetchBlockState(s, self).getPos(new Vector3i())); }

    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable collisionShape(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return aabbListToLua(s, fetchBlockState(s, self).getCollisionShape()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable outlineShape(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return aabbListToLua(s, fetchBlockState(s, self).getOutlineShape()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean hasCollision(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).hasCollision(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean fullCube(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).isFullCube(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable properties(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return s.stringListToTable(fetchBlockState(s, self).getProperties()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable tags(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return s.stringifyListToTable(fetchBlockState(s, self).getTags()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static LuaTable fluidTags(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return s.stringifyListToTable(fetchBlockState(s, self).getFluidTags()); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean hasBlockEntity(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).hasBlockEntity(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static String stateString(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getStateString(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean conductsRedstone(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).conductsRedstone(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float friction(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getFriction(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float speedModifier(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getVelocityModifier(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float jumpModifier(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getJumpModifier(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float hardness(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getHardness(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static float blastResistance(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getBlastResistance(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static boolean opaque(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).isOpaque(); }

    @LuaExpose @LuaPassState @LuaDynamicField public static float lightBlocked(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).getLightBlocked(); }
    @LuaExpose @LuaPassState @LuaDynamicField public static boolean emissive(LuaRuntime s, BlockStateView<?> self) throws LuaError, LuaOOM { return fetchBlockState(s, self).usesEmissiveRendering(); }

    // Helper to get the MinecraftBlockState instance
    private static @NotNull MinecraftBlockState fetchBlockState(LuaState state, BlockStateView<?> blockStateView) throws LuaError, LuaOOM {
        @Nullable MinecraftBlockState blockState = blockStateView.getValue();
        if (blockState == null) throw new LuaError("Attempt to use a BlockStateView after it was revoked!", state.allocationTracker);
        // BlockState is still valid.
        return blockState;
    }

    private static LuaTable aabbListToLua(LuaRuntime s, List<AABB> aabbs) throws LuaOOM {
        return s.listToTable(aabbs, (r, cube) -> {
            LuaTable cubeTable = new LuaTable(2, 0, r.allocationTracker);
            cubeTable.rawset(1, Vec3API.wrap(new Vector3d(cube.x1(), cube.y1(), cube.z1()), r));
            cubeTable.rawset(2, Vec3API.wrap(new Vector3d(cube.x2(), cube.y2(), cube.z2()), r));
            return cubeTable;
        });
    }

}
