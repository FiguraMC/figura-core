package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.model.part.RiggedHierarchy;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.joml.Vector3d;
import org.joml.Vector4d;

@LuaTypeAPI(typeName = "RiggedHierarchy", wrappedClass = RiggedHierarchy.class)
public class RiggedHierarchyAPI {

    public static LuaUserdata wrap(RiggedHierarchy<?> hierarchy, LuaRuntime state) {
        return switch (hierarchy) {
            case FiguraModelPart figuraPart -> FiguraPartAPI.wrap(figuraPart, state);
            case VanillaRendering.ScriptVanillaPart scriptVanillaPart -> VanillaPartAPI.wrap(scriptVanillaPart, state);
            default -> new LuaUserdata(hierarchy, state.figuraMetatables.riggedHierarchy);
        };
    }

    // Properties that access the transform.
    // 0 args -> getter
    // 1 arg -> vector setter
    // more args -> number setter

    @LuaExpose @LuaReturnSelf public static Vector3d origin(RiggedHierarchy<?> self) { return self.getTransform().getOrigin().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static Vector3d totalOrigin(RiggedHierarchy<?> self) { return self.getTransform().totalOrigin().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void origin(RiggedHierarchy<?> self, Vector3d origin) { origin(self, (float) origin.x, (float) origin.y, (float) origin.z); }
    @LuaExpose @LuaReturnSelf public static void origin(RiggedHierarchy<?> self, float x, float y, float z) { self.getTransform().setOrigin(x, y, z); }

    @LuaExpose @LuaReturnSelf public static Vector3d rot(RiggedHierarchy<?> self) { return rad(self).mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static Vector3d totalRot(RiggedHierarchy<?> self) { return totalRad(self).mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static void rot(RiggedHierarchy<?> self, Vector3d degrees) { rot(self, (float) degrees.x, (float) degrees.y, (float) degrees.z); }
    @LuaExpose @LuaReturnSelf public static void rot(RiggedHierarchy<?> self, float x, float y, float z) { self.getTransform().setEulerDeg(x, y, z); }

    @LuaExpose @LuaReturnSelf public static Vector3d rad(RiggedHierarchy<?> self) { return self.getTransform().getEulerRad().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static Vector3d totalRad(RiggedHierarchy<?> self) { return self.getTransform().totalEulerRad().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void rad(RiggedHierarchy<?> self, Vector3d radians) { rad(self, (float) radians.x, (float) radians.y, (float) radians.z); }
    @LuaExpose @LuaReturnSelf public static void rad(RiggedHierarchy<?> self, float x, float y, float z) { self.getTransform().setEulerRad(x, y, z); }

    @LuaExpose @LuaReturnSelf public static Vector3d scale(RiggedHierarchy<?> self) { return self.getTransform().getScale().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static Vector3d totalScale(RiggedHierarchy<?> self) { return self.getTransform().totalScale().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void scale(RiggedHierarchy<?> self, Vector3d scale) { scale(self, (float) scale.x, (float) scale.y, (float) scale.z); }
    @LuaExpose @LuaReturnSelf public static void scale(RiggedHierarchy<?> self, float x, float y, float z) { self.getTransform().setScale(x, y, z); }

    @LuaExpose @LuaReturnSelf public static Vector3d pos(RiggedHierarchy<?> self) { return self.getTransform().getPosition().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static Vector3d totalPos(RiggedHierarchy<?> self) { return self.getTransform().totalPosition().get(new Vector3d()); }
    @LuaExpose @LuaReturnSelf public static void pos(RiggedHierarchy<?> self, Vector3d pos) { pos(self, (float) pos.x, (float) pos.y, (float) pos.z); }
    @LuaExpose @LuaReturnSelf public static void pos(RiggedHierarchy<?> self, float x, float y, float z) { self.getTransform().setPosition(x, y, z); }

    @LuaExpose @LuaReturnSelf public static Vector4d color(RiggedHierarchy<?> self) { return self.getTransform().getColor().get(new Vector4d()); }
    @LuaExpose @LuaReturnSelf public static void color(RiggedHierarchy<?> self, Vector4d color) { color(self, (float) color.x, (float) color.y, (float) color.z, (float) color.w); }
    @LuaExpose @LuaReturnSelf public static void color(RiggedHierarchy<?> self, float r, float g, float b) { color(self, r, g, b, 1.0f); }
    @LuaExpose @LuaReturnSelf public static void color(RiggedHierarchy<?> self, float r, float g, float b, float a) { self.getTransform().setColor(r, g, b, a); }

    // Visibility getter/setter
    @LuaExpose public static boolean vis(RiggedHierarchy<?> self) { return self.getTransform().getVisible(); }
    @LuaExpose @LuaReturnSelf public static void vis(RiggedHierarchy<?> self, boolean visible) { self.getTransform().setVisible(visible); }

}
