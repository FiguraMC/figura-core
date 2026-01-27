package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.part.Transformable;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.joml.Vector3d;
import org.joml.Vector4d;

// Changes are made through queued setters if needed.
// (Should this be handled at a higher level, maybe? Since some transformable things might not be related to rendering...?)
@LuaTypeAPI(typeName = "Transformable", wrappedClass = Transformable.class)
public class TransformableAPI {

    public static LuaUserdata wrap(Transformable transformable, LuaRuntime state) {
        return switch (transformable) {
            case FiguraModelPart figuraPart -> ModelPartAPI.wrap(figuraPart, state);
            case VanillaRendering.ScriptVanillaPart scriptVanillaPart -> VanillaPartAPI.wrap(scriptVanillaPart, state);
            default -> new LuaUserdata(transformable, state.figuraMetatables.transformable);
        };
    }

    // Properties that access the transform.
    // 0 args -> getter (TODO: Consider issues? Are there concerns with sync, setters/getters called on different threads out of order? Possible tearing?)
    // 1 arg -> vector setter
    // more args -> number setter

    @LuaExpose public static Vector3d origin(Transformable self) { return self.getTransform().getOrigin().get(new Vector3d()); }
    @LuaExpose public static Vector3d totalOrigin(Transformable self) { return self.getTransform().totalOrigin().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void origin(Transformable self, Vector3d origin) { origin(self, (float) origin.x, (float) origin.y, (float) origin.z); }
    @LuaExpose @LuaReturnSelf public static void origin(Transformable self, float x, float y, float z) { QueuedSetters.handle(() -> self.getTransform().setOrigin(x, y, z)); }

    @LuaExpose public static Vector3d rot(Transformable self) { return rad(self).mul(180 / Math.PI); }
    @LuaExpose public static Vector3d totalRot(Transformable self) { return totalRad(self).mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static void rot(Transformable self, Vector3d degrees) { rot(self, (float) degrees.x, (float) degrees.y, (float) degrees.z); }
    @LuaExpose @LuaReturnSelf public static void rot(Transformable self, float x, float y, float z) { QueuedSetters.handle(() -> self.getTransform().setEulerDeg(x, y, z)); }

    @LuaExpose public static Vector3d rad(Transformable self) { return self.getTransform().getEulerRad().get(new Vector3d()); }
    @LuaExpose public static Vector3d totalRad(Transformable self) { return self.getTransform().totalEulerRad().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void rad(Transformable self, Vector3d radians) { rad(self, (float) radians.x, (float) radians.y, (float) radians.z); }
    @LuaExpose @LuaReturnSelf public static void rad(Transformable self, float x, float y, float z) { QueuedSetters.handle(() -> self.getTransform().setEulerRad(x, y, z)); }

    @LuaExpose public static Vector3d scale(Transformable self) { return self.getTransform().getScale().get(new Vector3d()); }
    @LuaExpose public static Vector3d totalScale(Transformable self) { return self.getTransform().totalScale().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void scale(Transformable self, Vector3d scale) { scale(self, (float) scale.x, (float) scale.y, (float) scale.z); }
    @LuaExpose @LuaReturnSelf public static void scale(Transformable self, float x, float y, float z) { QueuedSetters.handle(() -> self.getTransform().setScale(x, y, z)); }

    @LuaExpose public static Vector3d pos(Transformable self) { return self.getTransform().getPosition().get(new Vector3d()); }
    @LuaExpose public static Vector3d totalPos(Transformable self) { return self.getTransform().totalPosition().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void pos(Transformable self, Vector3d pos) { pos(self, (float) pos.x, (float) pos.y, (float) pos.z); }
    @LuaExpose @LuaReturnSelf public static void pos(Transformable self, float x, float y, float z) { QueuedSetters.handle(() -> self.getTransform().setPosition(x, y, z)); }

    @LuaExpose public static Vector4d color(Transformable self) { return self.getTransform().getColor().get(new Vector4d()); }
    @LuaExpose @LuaReturnSelf public static void color(Transformable self, Vector4d color) { color(self, (float) color.x, (float) color.y, (float) color.z, (float) color.w); }
    @LuaExpose @LuaReturnSelf public static void color(Transformable self, float r, float g, float b) { color(self, r, g, b, 1.0f); }
    @LuaExpose @LuaReturnSelf public static void color(Transformable self, float r, float g, float b, float a) { QueuedSetters.handle(() -> self.getTransform().setColor(r, g, b, a)); }

    // Visibility getter/setter
    @LuaExpose public static boolean vis(Transformable self) { return self.getTransform().getVisible(); }
    @LuaExpose @LuaReturnSelf public static void vis(Transformable self, boolean visible) { QueuedSetters.handle(() -> self.getTransform().setVisible(visible)); }
    
}
