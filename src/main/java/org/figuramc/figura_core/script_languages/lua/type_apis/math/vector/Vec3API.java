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

@LuaTypeAPI(typeName = "Vec3", wrappedClass = Vector3d.class)
public class Vec3API {

    public static LuaUserdata wrap(Vector3d vec, LuaRuntime state) throws LuaOOM {
        if (state.allocationTracker != null)
            state.allocationTracker.track(vec, AllocationTracker.OBJECT_SIZE + AllocationTracker.DOUBLE_SIZE * 3);
        return new LuaUserdata(vec, state.figuraMetatables.vec3);
    }

    // Mutating operations
    @LuaExpose @LuaReturnSelf public static void set(Vector3d self) { self.zero(); }
    @LuaExpose @LuaReturnSelf public static void set(Vector3d self, Vector3d other) { self.set(other); }
    @LuaExpose @LuaReturnSelf public static void set(Vector3d self, double x, double y, double z) { self.set(x, y, z); }
    @LuaExpose @LuaReturnSelf public static void setX(Vector3d self, double x) { self.x = x; }
    @LuaExpose @LuaReturnSelf public static void setY(Vector3d self, double y) { self.y = y; }
    @LuaExpose @LuaReturnSelf public static void setZ(Vector3d self, double z) { self.z = z; }
    @LuaExpose @LuaReturnSelf public static void setAll(Vector3d self, double v) { self.set(v); }

    @LuaExpose @LuaReturnSelf public static void add(Vector3d self, Vector3d other) { self.add(other); }
    @LuaExpose @LuaReturnSelf public static void add(Vector3d self, double x, double y, double z) { self.add(x, y, z); }
    @LuaExpose @LuaReturnSelf public static void addX(Vector3d self, double x) { self.x += x; }
    @LuaExpose @LuaReturnSelf public static void addY(Vector3d self, double y) { self.y += y; }
    @LuaExpose @LuaReturnSelf public static void addZ(Vector3d self, double z) { self.z += z; }
    @LuaExpose @LuaReturnSelf public static void addAll(Vector3d self, double v) { self.add(v, v, v); }

    @LuaExpose @LuaReturnSelf public static void sub(Vector3d self, Vector3d other) { self.sub(other); }
    @LuaExpose @LuaReturnSelf public static void sub(Vector3d self, double x, double y, double z) { self.sub(x, y, z); }
    @LuaExpose @LuaReturnSelf public static void subX(Vector3d self, double x) { self.x -= x; }
    @LuaExpose @LuaReturnSelf public static void subY(Vector3d self, double y) { self.y -= y; }
    @LuaExpose @LuaReturnSelf public static void subZ(Vector3d self, double z) { self.z -= z; }
    @LuaExpose @LuaReturnSelf public static void subAll(Vector3d self, double v) { self.sub(v, v, v); }

    @LuaExpose @LuaReturnSelf public static void mul(Vector3d self, Vector3d other) { self.mul(other); }
    @LuaExpose @LuaReturnSelf public static void mul(Vector3d self, double x, double y, double z) { self.x *= x; self.y *= y; self.z *= z; }
    @LuaExpose @LuaReturnSelf public static void mulX(Vector3d self, double x) { self.x *= x; }
    @LuaExpose @LuaReturnSelf public static void mulY(Vector3d self, double y) { self.y *= y; }
    @LuaExpose @LuaReturnSelf public static void mulZ(Vector3d self, double z) { self.z *= z; }
    @LuaExpose @LuaReturnSelf public static void mulAll(Vector3d self, double v) { self.mul(v); }

    @LuaExpose @LuaReturnSelf public static void div(Vector3d self, Vector3d other) { self.set(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y), OperationHelper.div(self.z, other.z)); }
    @LuaExpose @LuaReturnSelf public static void div(Vector3d self, double x, double y, double z) { self.set(OperationHelper.div(self.x, x), OperationHelper.div(self.y, y), OperationHelper.div(self.z, z)); }
    @LuaExpose @LuaReturnSelf public static void divX(Vector3d self, double x) { self.x = OperationHelper.div(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void divY(Vector3d self, double y) { self.y = OperationHelper.div(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void divZ(Vector3d self, double z) { self.z = OperationHelper.div(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void divAll(Vector3d self, double v) { div(self, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void mod(Vector3d self, Vector3d other) { self.set(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y), OperationHelper.mod(self.z, other.z)); }
    @LuaExpose @LuaReturnSelf public static void mod(Vector3d self, double x, double y, double z) { self.set(OperationHelper.mod(self.x, x), OperationHelper.mod(self.y, y), OperationHelper.mod(self.z, z)); }
    @LuaExpose @LuaReturnSelf public static void modX(Vector3d self, double x) { self.x = OperationHelper.mod(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void modY(Vector3d self, double y) { self.y = OperationHelper.mod(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void modZ(Vector3d self, double z) { self.z = OperationHelper.mod(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void modAll(Vector3d self, double v) { mod(self, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void max(Vector3d self, Vector3d other) { self.max(other); }
    @LuaExpose @LuaReturnSelf public static void max(Vector3d self, double x, double y, double z) { self.set(Math.max(self.x, x),Math.max(self.y, y),Math.max(self.z, z)); }
    @LuaExpose @LuaReturnSelf public static void maxX(Vector3d self, double x) { if (self.x < x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void maxY(Vector3d self, double y) { if (self.y < y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void maxZ(Vector3d self, double z) { if (self.z < z) self.z = z; }
    @LuaExpose @LuaReturnSelf public static void maxAll(Vector3d self, double v) { max(self, v, v, v); }

    @LuaExpose @LuaReturnSelf public static void min(Vector3d self, Vector3d other) { self.min(other); }
    @LuaExpose @LuaReturnSelf public static void min(Vector3d self, double x, double y, double z) { self.set(Math.min(self.x, x),Math.min(self.y, y),Math.min(self.z, z)); }
    @LuaExpose @LuaReturnSelf public static void minX(Vector3d self, double x) { if (self.x > x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void minY(Vector3d self, double y) { if (self.y > y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void minZ(Vector3d self, double z) { if (self.z > z) self.z = z; }
    @LuaExpose @LuaReturnSelf public static void minAll(Vector3d self, double v) { min(self, v, v, v); }

    // We'll say result is undefined if min > max
    @LuaExpose @LuaReturnSelf public static void clamp(Vector3d self, Vector3d min, Vector3d max) { self.max(min); self.min(max); }
    @LuaExpose @LuaReturnSelf public static void clamp(Vector3d self, double x1, double y1, double z1, double x2, double y2, double z2) { self.set(Math.clamp(self.x, x1, x2), Math.clamp(self.y, y1, y2), Math.clamp(self.z, z1, z2)); }
    @LuaExpose @LuaReturnSelf public static void clampX(Vector3d self, double x1, double x2) { if (self.x < x1) self.x = x1; else if (self.x > x2) self.x = x2; }
    @LuaExpose @LuaReturnSelf public static void clampY(Vector3d self, double y1, double y2) { if (self.y < y1) self.y = y1; else if (self.y > y2) self.y = y2; }
    @LuaExpose @LuaReturnSelf public static void clampZ(Vector3d self, double z1, double z2) { if (self.z < z1) self.z = z1; else if (self.z > z2) self.z = z2; }
    @LuaExpose @LuaReturnSelf public static void clampAll(Vector3d self, double v1, double v2) { clamp(self, v1, v1, v1, v2, v2, v2); }

    @LuaExpose @LuaReturnSelf public static void neg(Vector3d self) { self.negate(); }
    @LuaExpose @LuaReturnSelf public static void negX(Vector3d self) { self.x = -self.x; }
    @LuaExpose @LuaReturnSelf public static void negY(Vector3d self) { self.y = -self.y; }
    @LuaExpose @LuaReturnSelf public static void negZ(Vector3d self) { self.z = -self.z; }

    @LuaExpose @LuaReturnSelf public static void abs(Vector3d self) { self.absolute(); }
    @LuaExpose @LuaReturnSelf public static void absX(Vector3d self) { self.x = Math.abs(self.x); }
    @LuaExpose @LuaReturnSelf public static void absY(Vector3d self) { self.y = Math.abs(self.y); }
    @LuaExpose @LuaReturnSelf public static void absZ(Vector3d self) { self.z = Math.abs(self.z); }

    @LuaExpose @LuaReturnSelf public static void deg(Vector3d self) { self.mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static void rad(Vector3d self) { self.mul(Math.PI / 180); }

    @LuaExpose @LuaReturnSelf public static void normalize(Vector3d self) { self.normalize(); }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector3d self, double min) {
        double len2 = self.lengthSquared();
        if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector3d self, double min, double max) {
        double len2 = self.lengthSquared();
        if (len2 > max*max) self.normalize(max);
        else if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void floor(Vector3d self) { self.floor(); }
    @LuaExpose @LuaReturnSelf public static void ceil(Vector3d self) { self.ceil(); }
    @LuaExpose @LuaReturnSelf public static void round(Vector3d self) { self.round(); }
    @LuaExpose @LuaReturnSelf public static void lerp(Vector3d self, Vector3d other, double amount) { self.lerp(other, amount); }

    // Non-mutating operations or operations returning a different type
    @LuaExpose public static double dot(Vector3d self, Vector3d other) { return self.dot(other); }
    @LuaExpose public static double dot(Vector3d self, double x, double y, double z) { return self.dot(x, y, z); }
    @LuaExpose public static Vector3d copy(Vector3d self) { return new Vector3d(self); }
    @LuaExpose public static double len(Vector3d self) { return self.length(); }
    @LuaExpose public static double len2(Vector3d self) { return self.lengthSquared(); }
    @LuaExpose public static Varargs unpack(Vector3d self) { return ValueFactory.varargsOf(LuaDouble.valueOf(self.x), LuaDouble.valueOf(self.y), LuaDouble.valueOf(self.z)); }
    @LuaExpose public static double maxElem(Vector3d self) { return Math.max(self.x, Math.max(self.y, self.z)); }
    @LuaExpose public static double minElem(Vector3d self) { return Math.min(self.x, Math.min(self.y, self.z)); }

    // The Vector3 specials :3
    @LuaExpose @LuaReturnSelf public static void cross(Vector3d self, Vector3d other) { self.cross(other); }
    @LuaExpose @LuaReturnSelf public static void cross(Vector3d self, double x, double y, double z) { self.cross(x, y, z); }


    // Binary operator overloading.
    // Needs extra logic since we want to allow Vector + Vector and Vector + number.
    @LuaExpose @LuaPassState public static Vector3d __add(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return new Vector3d(self).add(other);
        double num = unknown.checkDouble(s);
        return new Vector3d(self).add(num, num, num);
    }
    @LuaExpose @LuaPassState public static Vector3d __sub(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return new Vector3d(self).sub(other);
        double num = unknown.checkDouble(s);
        return new Vector3d(self).sub(num, num, num);
    }
    @LuaExpose @LuaPassState public static Vector3d __mul(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return new Vector3d(self).mul(other);
        double num = unknown.checkDouble(s);
        return new Vector3d(self).mul(num);
    }
    @LuaExpose @LuaPassState public static Vector3d __div(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return new Vector3d(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y), OperationHelper.div(self.z, other.z));
        double num = unknown.checkDouble(s);
        return new Vector3d(OperationHelper.div(self.x, num), OperationHelper.div(self.y, num), OperationHelper.div(self.z, num));
    }
    @LuaExpose @LuaPassState public static Vector3d __mod(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return new Vector3d(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y), OperationHelper.mod(self.z, other.z));
        double num = unknown.checkDouble(s);
        return new Vector3d(OperationHelper.mod(self.x, num), OperationHelper.mod(self.y, num), OperationHelper.mod(self.z, num));
    }
    @LuaExpose @LuaPassState public static boolean __eq(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return self.equals(other);
        double num = unknown.checkDouble(s);
        return self.equals(num, num, num);
    }
    @LuaExpose @LuaPassState public static boolean __lt(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return self.x < other.x && self.y < other.y && self.z < other.z;
        double num = unknown.checkDouble(s);
        return self.x < num && self.y < num && self.z < num;
    }
    @LuaExpose @LuaPassState public static boolean __le(LuaRuntime s, Vector3d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector3d other)
            return self.x <= other.x && self.y <= other.y && self.z <= other.z;
        double num = unknown.checkDouble(s);
        return self.x <= num && self.y <= num && self.z <= num;
    }

    // Unary ops
    @LuaExpose public static Vector3d __unm(Vector3d self) { return self.negate(new Vector3d()); }
    @LuaExpose public static double __len(Vector3d self) { return self.length(); }

    // Swizzling! :D

    @LuaExpose @LuaPassState public static LuaValue __index(LuaRuntime s, Vector3d self, LuaValue key) throws LuaError, LuaOOM {
        // Numeric key, index it like an array
        if (key.type() == TNUMBER) {
            return switch (key.toInteger()) {
                case 1 -> LuaDouble.valueOf(self.x);
                case 2 -> LuaDouble.valueOf(self.y);
                case 3 -> LuaDouble.valueOf(self.z);
                default -> NIL;
            };
        }
        // Key should be a string then
        LuaString k = key.checkLuaString(s);
        // Get the swizzle going
        try {
            return switch (k.length()) {
                case 1 -> k.charAt(0) == 'c' ? Vec3API.wrap(new Vector3d(self), s) : LuaDouble.valueOf(getSwizzle(self, (char) k.charAt(0)));
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

    @LuaExpose @LuaPassState public static void __newindex(LuaRuntime s, Vector3d self, LuaValue key, LuaValue value) throws LuaError, LuaOOM {
        // Numeric key, index like an array
        if (key.type() == TNUMBER) {
            switch (key.toInteger()) {
                case 1 -> self.x = value.checkDouble(s);
                case 2 -> self.y = value.checkDouble(s);
                case 3 -> self.z = value.checkDouble(s);
                default -> throw ErrorFactory.argError(s, key, "integer 1 to 3");
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

    private static double getSwizzle(Vector3d v, char c) throws InvalidSwizzleException {
        return switch (c) {
            case '_' -> 0;
            case 'i' -> 1;
            case 'I' -> -1;
            case 'x', 'r', 'u' -> v.x;
            case 'y', 'g', 'v' -> v.y;
            case 'z', 'b' -> v.z;
            case 'X', 'R', 'U' -> -v.x;
            case 'Y', 'G', 'V' -> -v.y;
            case 'Z', 'B' -> -v.z;
            default -> throw InvalidSwizzleException.INSTANCE; // Signal to caller to return nil
        };
    }
    private static void setSwizzle(Vector3d v, char c, double num, LuaState state) throws LuaError, LuaOOM {
        switch (c) {
            case '_' -> {}
            case 'x', 'r', 'u' -> v.x = num;
            case 'y', 'g', 'v' -> v.y = num;
            case 'z', 'b' -> v.z = num;
            case 'X', 'R', 'U' -> v.x = -num;
            case 'Y', 'G', 'V' -> v.y = -num;
            case 'Z', 'B' -> v.z = -num;
            default -> throw new LuaError("Invalid swizzle character to Vec3: '" + c + "'", state.allocationTracker); // Swizzle assignment should error on invalid
        }
    }

    private static class InvalidSwizzleException extends Exception {
        private static final InvalidSwizzleException INSTANCE = new InvalidSwizzleException();
    }

}
