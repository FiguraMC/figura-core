package org.figuramc.figura_core.script_languages.lua.type_apis.math.vector;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.memory_tracker.AllocationTracker;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector4d;

import static org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants.NIL;
import static org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants.TNUMBER;

@LuaTypeAPI(typeName = "Vec4", wrappedClass = Vector4d.class)
public class Vec4API {

    public static LuaUserdata wrap(Vector4d vec, LuaRuntime state) throws LuaOOM {
        if (state.allocationTracker != null)
            state.allocationTracker.track(vec, AllocationTracker.OBJECT_SIZE + AllocationTracker.DOUBLE_SIZE * 4);
        return new LuaUserdata(vec, state.figuraMetatables.vec4);
    }

    // Mutating operations
    @LuaExpose @LuaReturnSelf public static void set(Vector4d self) { self.zero(); }
    @LuaExpose @LuaReturnSelf public static void set(Vector4d self, Vector4d other) { self.set(other); }
    @LuaExpose @LuaReturnSelf public static void set(Vector4d self, double x, double y, double z, double w) { self.set(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void setX(Vector4d self, double x) { self.x = x; }
    @LuaExpose @LuaReturnSelf public static void setY(Vector4d self, double y) { self.y = y; }
    @LuaExpose @LuaReturnSelf public static void setZ(Vector4d self, double z) { self.z = z; }
    @LuaExpose @LuaReturnSelf public static void setW(Vector4d self, double w) { self.w = w; }
    @LuaExpose @LuaReturnSelf public static void setAll(Vector4d self, double v) { self.set(v); }

    @LuaExpose @LuaReturnSelf public static void add(Vector4d self, Vector4d other) { self.add(other); }
    @LuaExpose @LuaReturnSelf public static void add(Vector4d self, double x, double y, double z, double w) { self.add(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void addX(Vector4d self, double x) { self.x += x; }
    @LuaExpose @LuaReturnSelf public static void addY(Vector4d self, double y) { self.y += y; }
    @LuaExpose @LuaReturnSelf public static void addZ(Vector4d self, double z) { self.z += z; }
    @LuaExpose @LuaReturnSelf public static void addW(Vector4d self, double w) { self.w += w; }
    @LuaExpose @LuaReturnSelf public static void addAll(Vector4d self, double v) { self.add(v, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void sub(Vector4d self, Vector4d other) { self.sub(other); }
    @LuaExpose @LuaReturnSelf public static void sub(Vector4d self, double x, double y, double z, double w) { self.sub(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void subX(Vector4d self, double x) { self.x -= x; }
    @LuaExpose @LuaReturnSelf public static void subY(Vector4d self, double y) { self.y -= y; }
    @LuaExpose @LuaReturnSelf public static void subZ(Vector4d self, double z) { self.z -= z; }
    @LuaExpose @LuaReturnSelf public static void subW(Vector4d self, double w) { self.w -= w; }
    @LuaExpose @LuaReturnSelf public static void subAll(Vector4d self, double v) { self.sub(v, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void mul(Vector4d self, Vector4d other) { self.mul(other); }
    @LuaExpose @LuaReturnSelf public static void mul(Vector4d self, double x, double y, double z, double w) { self.x *= x; self.y *= y; self.z *= z; self.w *= w; }
    @LuaExpose @LuaReturnSelf public static void mulX(Vector4d self, double x) { self.x *= x; }
    @LuaExpose @LuaReturnSelf public static void mulY(Vector4d self, double y) { self.y *= y; }
    @LuaExpose @LuaReturnSelf public static void mulZ(Vector4d self, double z) { self.z *= z; }
    @LuaExpose @LuaReturnSelf public static void mulW(Vector4d self, double w) { self.w *= w; }
    @LuaExpose @LuaReturnSelf public static void mulAll(Vector4d self, double v) { self.mul(v); }

    @LuaExpose @LuaReturnSelf public static void div(Vector4d self, Vector4d other) { self.set(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y), OperationHelper.div(self.z, other.z), OperationHelper.div(self.w, other.w)); }
    @LuaExpose @LuaReturnSelf public static void div(Vector4d self, double x, double y, double z, double w) { self.set(OperationHelper.div(self.x, x), OperationHelper.div(self.y, y), OperationHelper.div(self.z, z), OperationHelper.div(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void divX(Vector4d self, double x) { self.x = OperationHelper.div(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void divY(Vector4d self, double y) { self.y = OperationHelper.div(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void divZ(Vector4d self, double z) { self.z = OperationHelper.div(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void divW(Vector4d self, double w) { self.w = OperationHelper.div(self.x, w); }
    @LuaExpose @LuaReturnSelf public static void divAll(Vector4d self, double v) { div(self, v, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void mod(Vector4d self, Vector4d other) { self.set(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y), OperationHelper.mod(self.z, other.z), OperationHelper.mod(self.w, other.w)); }
    @LuaExpose @LuaReturnSelf public static void mod(Vector4d self, double x, double y, double z, double w) { self.set(OperationHelper.mod(self.x, x), OperationHelper.mod(self.y, y), OperationHelper.mod(self.z, z), OperationHelper.mod(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void modX(Vector4d self, double x) { self.x = OperationHelper.mod(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void modY(Vector4d self, double y) { self.y = OperationHelper.mod(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void modZ(Vector4d self, double z) { self.z = OperationHelper.mod(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void modW(Vector4d self, double w) { self.w = OperationHelper.mod(self.x, w); }
    @LuaExpose @LuaReturnSelf public static void modAll(Vector4d self, double v) { mod(self, v, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void max(Vector4d self, Vector4d other) { self.max(other); }
    @LuaExpose @LuaReturnSelf public static void max(Vector4d self, double x, double y, double z, double w) { self.set(Math.max(self.x, x),Math.max(self.y, y),Math.max(self.z, z),Math.max(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void maxX(Vector4d self, double x) { if (self.x < x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void maxY(Vector4d self, double y) { if (self.y < y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void maxZ(Vector4d self, double z) { if (self.z < z) self.z = z; }
    @LuaExpose @LuaReturnSelf public static void maxW(Vector4d self, double w) { if (self.w < w) self.w = w; }
    @LuaExpose @LuaReturnSelf public static void maxAll(Vector4d self, double v) { max(self, v, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void min(Vector4d self, Vector4d other) { self.min(other); }
    @LuaExpose @LuaReturnSelf public static void min(Vector4d self, double x, double y, double z, double w) { self.set(Math.min(self.x, x),Math.min(self.y, y),Math.min(self.z, z),Math.min(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void minX(Vector4d self, double x) { if (self.x > x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void minY(Vector4d self, double y) { if (self.y > y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void minZ(Vector4d self, double z) { if (self.z > z) self.z = z; }
    @LuaExpose @LuaReturnSelf public static void minW(Vector4d self, double w) { if (self.w > w) self.w = w; }
    @LuaExpose @LuaReturnSelf public static void minAll(Vector4d self, double v) { min(self, v, v, v, v); }

    // We'll say result is undefined if min > max
    @LuaExpose @LuaReturnSelf public static void clamp(Vector4d self, Vector4d min, Vector4d max) { self.max(min); self.min(max); }
    @LuaExpose @LuaReturnSelf public static void clamp(Vector4d self, double x1, double y1, double z1, double w1, double x2, double y2, double z2, double w2) { self.set(Math.clamp(self.x, x1, x2), Math.clamp(self.y, y1, y2), Math.clamp(self.z, z1, z2), Math.clamp(self.w, w1, w2)); }
    @LuaExpose @LuaReturnSelf public static void clampX(Vector4d self, double x1, double x2) { if (self.x < x1) self.x = x1; else if (self.x > x2) self.x = x2; }
    @LuaExpose @LuaReturnSelf public static void clampY(Vector4d self, double y1, double y2) { if (self.y < y1) self.y = y1; else if (self.y > y2) self.y = y2; }
    @LuaExpose @LuaReturnSelf public static void clampZ(Vector4d self, double z1, double z2) { if (self.z < z1) self.z = z1; else if (self.z > z2) self.z = z2; }
    @LuaExpose @LuaReturnSelf public static void clampW(Vector4d self, double w1, double w2) { if (self.w < w1) self.w = w1; else if (self.w > w2) self.w = w2; }
    @LuaExpose @LuaReturnSelf public static void clampAll(Vector4d self, double v1, double v2) { clamp(self, v1, v1, v1, v1, v2, v2, v2, v2); }

    @LuaExpose @LuaReturnSelf public static void neg(Vector4d self) { self.negate(); }
    @LuaExpose @LuaReturnSelf public static void negX(Vector4d self) { self.x = -self.x; }
    @LuaExpose @LuaReturnSelf public static void negY(Vector4d self) { self.y = -self.y; }
    @LuaExpose @LuaReturnSelf public static void negZ(Vector4d self) { self.z = -self.z; }
    @LuaExpose @LuaReturnSelf public static void negW(Vector4d self) { self.w = -self.w; }

    @LuaExpose @LuaReturnSelf public static void abs(Vector4d self) { self.absolute(); }
    @LuaExpose @LuaReturnSelf public static void absX(Vector4d self) { self.x = Math.abs(self.x); }
    @LuaExpose @LuaReturnSelf public static void absY(Vector4d self) { self.y = Math.abs(self.y); }
    @LuaExpose @LuaReturnSelf public static void absZ(Vector4d self) { self.z = Math.abs(self.z); }
    @LuaExpose @LuaReturnSelf public static void absW(Vector4d self) { self.w = Math.abs(self.w); }

    @LuaExpose @LuaReturnSelf public static void deg(Vector4d self) { self.mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static void rad(Vector4d self) { self.mul(Math.PI / 180); }

    @LuaExpose @LuaReturnSelf public static void normalize(Vector4d self) { self.normalize(); }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector4d self, double min) {
        double len2 = self.lengthSquared();
        if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector4d self, double min, double max) {
        double len2 = self.lengthSquared();
        if (len2 > max*max) self.normalize(max);
        else if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void floor(Vector4d self) { self.floor(); }
    @LuaExpose @LuaReturnSelf public static void ceil(Vector4d self) { self.ceil(); }
    @LuaExpose @LuaReturnSelf public static void round(Vector4d self) { self.round(); }
    @LuaExpose @LuaReturnSelf public static void lerp(Vector4d self, Vector4d other, double amount) { self.lerp(other, amount); }

    // Non-mutating operations or operations returning a different type
    @LuaExpose public static double dot(Vector4d self, Vector4d other) { return self.dot(other); }
    @LuaExpose public static double dot(Vector4d self, double x, double y, double z, double w) { return self.dot(x, y, z, w); }
    @LuaExpose public static Vector4d copy(Vector4d self) { return new Vector4d(self); }
    @LuaExpose public static double len(Vector4d self) { return self.length(); }
    @LuaExpose public static double len2(Vector4d self) { return self.lengthSquared(); }
    @LuaExpose public static Varargs unpack(Vector4d self) { return ValueFactory.varargsOf(LuaDouble.valueOf(self.x), LuaDouble.valueOf(self.y), LuaDouble.valueOf(self.z), LuaDouble.valueOf(self.w)); }
    @LuaExpose public static double maxElem(Vector4d self) { return Math.max(Math.max(self.x, self.y), Math.max(self.z, self.w)); }
    @LuaExpose public static double minElem(Vector4d self) { return Math.min(Math.min(self.x, self.y), Math.min(self.z, self.w)); }

    // Binary operator overloading.
    // Needs extra logic since we want to allow Vector + Vector, Vector + number, and number + Vector.
    @LuaExpose @LuaPassState public static Vector4d __add(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            // Vector + ?
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                // Vector + Vector
                return new Vector4d(vec1).add(vec2);
            // Vector + Number
            double num = arg2.checkDouble(s);
            return new Vector4d(vec1).add(num, num, num, num);
        } else {
            // Number + Vector
            double num = arg1.checkDouble(s);
            return new Vector4d(num).add(arg2.checkUserdata(s, Vector4d.class));
        }
    }
    @LuaExpose @LuaPassState public static Vector4d __sub(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return new Vector4d(vec1).sub(vec2);
            double num = arg2.checkDouble(s);
            return new Vector4d(vec1).sub(num, num, num, num);
        } else {
            double num = arg1.checkDouble(s);
            return new Vector4d(num).sub(arg2.checkUserdata(s, Vector4d.class));
        }
    }
    @LuaExpose @LuaPassState public static Vector4d __mul(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return new Vector4d(vec1).mul(vec2);
            double num = arg2.checkDouble(s);
            return new Vector4d(vec1).mul(num);
        } else {
            double num = arg1.checkDouble(s);
            return new Vector4d(arg2.checkUserdata(s, Vector4d.class)).mul(num);
        }
    }
    @LuaExpose @LuaPassState public static Vector4d __div(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return new Vector4d(OperationHelper.div(vec1.x, vec2.x), OperationHelper.div(vec1.y, vec2.y), OperationHelper.div(vec1.z, vec2.z), OperationHelper.div(vec1.w, vec2.w));
            double num = arg2.checkDouble(s);
            return new Vector4d(OperationHelper.div(vec1.x, num), OperationHelper.div(vec1.y, num), OperationHelper.div(vec1.z, num), OperationHelper.div(vec1.w, num));
        } else {
            double num = arg1.checkDouble(s);
            Vector4d vec2 = arg2.checkUserdata(s, Vector4d.class);
            return new Vector4d(OperationHelper.div(num, vec2.x), OperationHelper.div(num, vec2.y), OperationHelper.div(num, vec2.z), OperationHelper.div(num, vec2.w));
        }
    }
    @LuaExpose @LuaPassState public static Vector4d __mod(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return new Vector4d(OperationHelper.mod(vec1.x, vec2.x), OperationHelper.mod(vec1.y, vec2.y), OperationHelper.mod(vec1.z, vec2.z), OperationHelper.mod(vec1.w, vec2.w));
            double num = arg2.checkDouble(s);
            return new Vector4d(OperationHelper.mod(vec1.x, num), OperationHelper.mod(vec1.y, num), OperationHelper.mod(vec1.z, num), OperationHelper.mod(vec1.w, num));
        } else {
            double num = arg1.checkDouble(s);
            Vector4d vec2 = arg2.checkUserdata(s, Vector4d.class);
            return new Vector4d(OperationHelper.mod(num, vec2.x), OperationHelper.mod(num, vec2.y), OperationHelper.mod(num, vec2.z), OperationHelper.mod(num, vec2.w));
        }
    }
    @LuaExpose @LuaPassState public static boolean __eq(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return vec1.equals(vec2);
            double num = arg2.checkDouble(s);
            return vec1.equals(num, num, num, num);
        } else {
            double num = arg1.checkDouble(s);
            Vector4d vec2 = arg2.checkUserdata(s, Vector4d.class);
            return vec2.equals(num, num, num, num);
        }
    }
    @LuaExpose @LuaPassState public static boolean __lt(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return vec1.x < vec2.x && vec1.y < vec2.y && vec1.z < vec2.z && vec1.w < vec2.w;
            double num = arg2.checkDouble(s);
            return vec1.x < num && vec1.y < num && vec1.z < num && vec1.w < num;
        } else {
            double num = arg1.checkDouble(s);
            Vector4d vec2 = arg2.checkUserdata(s, Vector4d.class);
            return num < vec2.x && num < vec2.y && num < vec2.z && num < vec2.w;
        }
    }
    @LuaExpose @LuaPassState public static boolean __le(LuaRuntime s, LuaValue arg1, LuaValue arg2) throws LuaError, LuaOOM {
        if (arg1 instanceof LuaUserdata userdata1 && userdata1.userdata() instanceof Vector4d vec1) {
            if (arg2 instanceof LuaUserdata userdata2 && userdata2.userdata() instanceof Vector4d vec2)
                return vec1.x <= vec2.x && vec1.y <= vec2.y && vec1.z <= vec2.z && vec1.w <= vec2.w;
            double num = arg2.checkDouble(s);
            return vec1.x <= num && vec1.y <= num && vec1.z <= num && vec1.w <= num;
        } else {
            double num = arg1.checkDouble(s);
            Vector4d vec2 = arg2.checkUserdata(s, Vector4d.class);
            return num <= vec2.x && num <= vec2.y && num <= vec2.z && num <= vec2.w;
        }
    }

    // Unary ops
    @LuaExpose public static Vector4d __unm(Vector4d self) { return self.negate(new Vector4d()); }
    @LuaExpose public static double __len(Vector4d self) { return self.length(); }

    // Swizzling! :D

    @LuaExpose @LuaPassState public static LuaValue __index(LuaRuntime s, Vector4d self, LuaValue key) throws LuaError, LuaOOM {
        // Numeric key, index it like an array
        if (key.type() == TNUMBER) {
            return switch (key.toInteger()) {
                case 1 -> LuaDouble.valueOf(self.x);
                case 2 -> LuaDouble.valueOf(self.y);
                case 3 -> LuaDouble.valueOf(self.z);
                case 4 -> LuaDouble.valueOf(self.w);
                default -> NIL;
            };
        }
        // Key should be a string then
        LuaString k = key.checkLuaString(s);
        // Get the swizzle going
        try {
            return switch (k.length()) {
                case 1 -> k.charAt(0) == 'c' ? Vec4API.wrap(new Vector4d(self), s) : LuaDouble.valueOf(getSwizzle(self, (char) k.charAt(0)));
                case 2 -> Vec2API.wrap(new Vector2d(
                        getSwizzle(self, (char) k.charAt(0)),
                        getSwizzle(self, (char) k.charAt(1))
                ), s);
                case 3 -> Vec3API.wrap(new Vector3d(
                        getSwizzle(self, (char) k.charAt(0)),
                        getSwizzle(self, (char) k.charAt(1)),
                        getSwizzle(self, (char) k.charAt(2))
                ), s);
                case 4 -> Vec4API.wrap(new Vector4d(
                        getSwizzle(self, (char) k.charAt(0)),
                        getSwizzle(self, (char) k.charAt(1)),
                        getSwizzle(self, (char) k.charAt(2)),
                        getSwizzle(self, (char) k.charAt(3))
                ), s);
                default -> NIL; // Not a swizzle, return nil
            };
        } catch (InvalidSwizzleException ex) {
            // If swizzle failed, just return nil
            // This path should be uncommon, so using exceptions for control flow should be okay
            return NIL;
        }
    }

    @LuaExpose @LuaPassState public static void __newindex(LuaRuntime s, Vector4d self, LuaValue key, LuaValue value) throws LuaError, LuaOOM {
        // Numeric key, index like an array
        if (key.type() == TNUMBER) {
            switch (key.toInteger()) {
                case 1 -> self.x = value.checkDouble(s);
                case 2 -> self.y = value.checkDouble(s);
                case 3 -> self.z = value.checkDouble(s);
                case 4 -> self.w = value.checkDouble(s);
                default -> throw ErrorFactory.argError(s, key, "integer 1 to 4");
            }
        }
        LuaString k = key.checkLuaString(s);
        switch (k.length()) {
            case 1 -> setSwizzle(self, (char) k.charAt(0), value.checkDouble(s), s);
            case 2 -> {
                Vector2d rhs = value.checkUserdata(s, Vector2d.class);
                setSwizzle(self, (char) k.charAt(0), rhs.x, s);
                setSwizzle(self, (char) k.charAt(1), rhs.y, s);
            }
            case 3 -> {
                Vector3d rhs = value.checkUserdata(s, Vector3d.class);
                setSwizzle(self, (char) k.charAt(0), rhs.x, s);
                setSwizzle(self, (char) k.charAt(1), rhs.y, s);
                setSwizzle(self, (char) k.charAt(2), rhs.z, s);
            }
            case 4 -> {
                Vector4d rhs = value.checkUserdata(s, Vector4d.class);
                setSwizzle(self, (char) k.charAt(0), rhs.x, s);
                setSwizzle(self, (char) k.charAt(1), rhs.y, s);
                setSwizzle(self, (char) k.charAt(2), rhs.z, s);
                setSwizzle(self, (char) k.charAt(3), rhs.w, s);
            }
            default -> throw new LuaError("Invalid swizzle - length must be 1 to 4 chars, got '" + k + "'", s.allocationTracker);
        }
    }

    private static double getSwizzle(Vector4d v, char c) throws InvalidSwizzleException {
        return switch (c) {
            case '_' -> 0;
            case 'i' -> 1;
            case 'I' -> -1;
            case 'x', 'r', 'u' -> v.x;
            case 'y', 'g', 'v' -> v.y;
            case 'z', 'b' -> v.z;
            case 'w', 'a' -> v.w;
            case 'X', 'R', 'U' -> -v.x;
            case 'Y', 'G', 'V' -> -v.y;
            case 'Z', 'B' -> -v.z;
            case 'W', 'A' -> -v.w;
            default -> throw InvalidSwizzleException.INSTANCE; // Signal to caller to return nil
        };
    }
    private static void setSwizzle(Vector4d v, char c, double num, LuaState state) throws LuaError, LuaOOM {
        switch (c) {
            case '_' -> {}
            case 'x', 'r', 'u' -> v.x = num;
            case 'y', 'g', 'v' -> v.y = num;
            case 'z', 'b' -> v.z = num;
            case 'w', 'a' -> v.w = num;
            case 'X', 'R', 'U' -> v.x = -num;
            case 'Y', 'G', 'V' -> v.y = -num;
            case 'Z', 'B' -> v.z = -num;
            case 'W', 'A' -> v.w = -num;
            default -> throw new LuaError("Invalid swizzle character to Vec4: '" + c + "'", state.allocationTracker); // Swizzle assignment should error on invalid
        }
    }

    private static class InvalidSwizzleException extends Exception {
        private static final InvalidSwizzleException INSTANCE = new InvalidSwizzleException();
    }

}
