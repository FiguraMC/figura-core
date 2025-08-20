package org.figuramc.figura_core.script_languages.lua.type_apis.world.entity;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@LuaTypeAPI(typeName = "Entity", wrappedClass = EntityView.class)
public class EntityViewAPI {

    public static LuaUserdata wrap(EntityView<?> entityView, LuaRuntime state) {
        return new LuaUserdata(entityView, state.figuraMetatables.entityView);
    }

    @LuaExpose @LuaPassState
    public static Vector3d pos(LuaState s, EntityView<?> self) throws LuaError, LuaUncatchableError {
        return fetchEntity(s, self).getPosition(1.0f, new Vector3d());
    }
    @LuaExpose @LuaPassState
    public static Vector3d pos(LuaState s, EntityView<?> self, float delta) throws LuaError, LuaUncatchableError {
        return fetchEntity(s, self).getPosition(delta, new Vector3d());
    }

    // Helper to fetch entity, or error if revoked
    private static @NotNull MinecraftEntity fetchEntity(LuaState state, EntityView<?> entityView) throws LuaError, LuaUncatchableError {
        // Get entity
        @Nullable MinecraftEntity entity = entityView.getEntity();
        // If null (aka revoked), error
        if (entity == null) throw new LuaError("Attempt to use entity view after it was revoked!", state.allocationTracker);
        // Return the non-null entity
        return entity;
    }

}
