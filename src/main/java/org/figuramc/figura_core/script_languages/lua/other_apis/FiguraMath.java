package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.ErrorFactory;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec2API;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec3API;
import org.figuramc.figura_core.script_languages.lua.type_apis.math.vector.Vec4API;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector4d;

/**
 * Set up the Figura Math functions, by running math.lua and adding some manually
 */
public class FiguraMath {

    public static void init(LuaState state) throws LuaError, LuaOOM, LuaUncatchableError {

        // Run math.lua
        LuaRuntime.runAssetFile(state, "math");

        // Math object creation functions

        // vec2(), vec3(), vec4(), creates a vec of the appropriate size, taking 0, 1, or N args.
        state.globals().rawset("vec2", LibFunction.createV((s, args) -> switch (args.count()) {
            case 0 -> Vec2API.wrap(new Vector2d(), (LuaRuntime) s);
            case 1 -> Vec2API.wrap(new Vector2d(args.arg(1).checkDouble(s)), (LuaRuntime) s);
            case 2 -> Vec2API.wrap(new Vector2d(args.arg(1).checkDouble(s), args.arg(2).checkDouble(s)), (LuaRuntime) s);
            default -> throw ErrorFactory.argCountError(s, "vec2()", args.count(), 0, 1, 2);
        }));
        state.globals().rawset("vec3", LibFunction.createV((s, args) -> switch (args.count()) {
            case 0 -> Vec3API.wrap(new Vector3d(), (LuaRuntime) s);
            case 1 -> Vec3API.wrap(new Vector3d(args.arg(1).checkDouble(s)), (LuaRuntime) s);
            case 3 -> Vec3API.wrap(new Vector3d(args.arg(1).checkDouble(s), args.arg(2).checkDouble(s), args.arg(3).checkDouble(s)), (LuaRuntime) s);
            default -> throw ErrorFactory.argCountError(s, "vec3()", args.count(), 0, 1, 3);
        }));
        state.globals().rawset("vec4", LibFunction.createV((s, args) -> switch (args.count()) {
            case 0 -> Vec4API.wrap(new Vector4d(), (LuaRuntime) s);
            case 1 -> Vec4API.wrap(new Vector4d(args.arg(1).checkDouble(s)), (LuaRuntime) s);
            case 4 -> Vec4API.wrap(new Vector4d(args.arg(1).checkDouble(s), args.arg(2).checkDouble(s), args.arg(3).checkDouble(s), args.arg(4).checkDouble(s)), (LuaRuntime) s);
            default -> throw ErrorFactory.argCountError(s, "vec4()", args.count(), 0, 1, 4);
        }));



    }


}
