package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaDouble;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec2API;
import org.joml.Vector2d;

/**
 * The 'client' table in Lua globals.
 */
public class ClientTable {

    public static LuaTable create(LuaRuntime state) throws LuaError, LuaUncatchableError {
        LuaTable client = new LuaTable(state.allocationTracker);

        // Since scaled values are much more useful, we prioritize those and prefix non-scaled values with "raw".
        client.rawset("windowSize", LibFunction.create(s -> {
            float[] res = FiguraConnectionPoint.GAME_DATA_PROVIDER.getScaledWindowSize();
            return Vec2API.wrap(new Vector2d(res[0], res[1]), (LuaRuntime) s);
        }));
        client.rawset("rawWindowSize", LibFunction.create(s -> {
            float[] res = FiguraConnectionPoint.GAME_DATA_PROVIDER.getWindowSize();
            return Vec2API.wrap(new Vector2d(res[0], res[1]), (LuaRuntime) s);
        }));
        client.rawset("mousePos", LibFunction.create(s -> {
            float[] res = FiguraConnectionPoint.GAME_DATA_PROVIDER.getScaledMousePosition();
            return Vec2API.wrap(new Vector2d(res[0], res[1]), (LuaRuntime) s);
        }));
        client.rawset("rawMousePos", LibFunction.create(s -> {
            float[] res = FiguraConnectionPoint.GAME_DATA_PROVIDER.getMousePosition();
            return Vec2API.wrap(new Vector2d(res[0], res[1]), (LuaRuntime) s);
        }));

        client.rawset("guiScale", LibFunction.create(s -> LuaDouble.valueOf(FiguraConnectionPoint.GAME_DATA_PROVIDER.getGuiScale())));

        return client;
    }

}
