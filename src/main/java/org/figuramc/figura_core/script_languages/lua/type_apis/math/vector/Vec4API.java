package org.figuramc.figura_core.script_languages.lua.type_apis.math.vector;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector4d;

import static org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants.NIL;
import static org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants.TNUMBER;

@LuaTypeAPI(typeName = "Vec4", wrappedClass = Vector4d.class)
public class Vec4API {

    // TODO make this use memory
    public static LuaUserdata wrap(Vector4d vec, LuaRuntime state) {
        return new LuaUserdata(vec, state.figuraMetatables.vec4);
    }

    // Mutating operations
    @LuaExpose @LuaReturnSelf public static void set(Vector4d self, Vector4d other) { self.set(other); }
    @LuaExpose @LuaReturnSelf public static void set(Vector4d self, double x, double y, double z, double w) { self.set(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void setX(Vector4d self, double x) { self.x = x; }
    @LuaExpose @LuaReturnSelf public static void setY(Vector4d self, double y) { self.y = y; }
    @LuaExpose @LuaReturnSelf public static void setZ(Vector4d self, double z) { self.z = z; }
    @LuaExpose @LuaReturnSelf public static void setW(Vector4d self, double w) { self.w = w; }

    @LuaExpose @LuaReturnSelf public static void add(Vector4d self, Vector4d other) { self.add(other); }
    @LuaExpose @LuaReturnSelf public static void add(Vector4d self, double x, double y, double z, double w) { self.add(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void addX(Vector4d self, double x) { self.x += x; }
    @LuaExpose @LuaReturnSelf public static void addY(Vector4d self, double y) { self.y += y; }
    @LuaExpose @LuaReturnSelf public static void addZ(Vector4d self, double z) { self.z += z; }
    @LuaExpose @LuaReturnSelf public static void addW(Vector4d self, double w) { self.w += w; }

    @LuaExpose @LuaReturnSelf public static void sub(Vector4d self, Vector4d other) { self.sub(other); }
    @LuaExpose @LuaReturnSelf public static void sub(Vector4d self, double x, double y, double z, double w) { self.sub(x, y, z, w); }
    @LuaExpose @LuaReturnSelf public static void subX(Vector4d self, double x) { self.x -= x; }
    @LuaExpose @LuaReturnSelf public static void subY(Vector4d self, double y) { self.y -= y; }
    @LuaExpose @LuaReturnSelf public static void subZ(Vector4d self, double z) { self.z -= z; }
    @LuaExpose @LuaReturnSelf public static void subW(Vector4d self, double w) { self.w -= w; }

    @LuaExpose @LuaReturnSelf public static void mul(Vector4d self, Vector4d other) { self.mul(other); }
    @LuaExpose @LuaReturnSelf public static void mul(Vector4d self, double x, double y, double z, double w) { self.x *= x; self.y *= y; self.z *= z; self.w *= w; }
    @LuaExpose @LuaReturnSelf public static void mulX(Vector4d self, double x) { self.x *= x; }
    @LuaExpose @LuaReturnSelf public static void mulY(Vector4d self, double y) { self.y *= y; }
    @LuaExpose @LuaReturnSelf public static void mulZ(Vector4d self, double z) { self.z *= z; }
    @LuaExpose @LuaReturnSelf public static void mulW(Vector4d self, double w) { self.w *= w; }

    @LuaExpose @LuaReturnSelf public static void div(Vector4d self, Vector4d other) {
        self.set(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y), OperationHelper.div(self.z, other.z), OperationHelper.div(self.w, other.w));
    }
    @LuaExpose @LuaReturnSelf public static void div(Vector4d self, double x, double y, double z, double w) {
        self.set(OperationHelper.div(self.x, x), OperationHelper.div(self.y, y), OperationHelper.div(self.z, z), OperationHelper.div(self.w, w));
    }
    @LuaExpose @LuaReturnSelf public static void divX(Vector4d self, double x) { self.x = OperationHelper.div(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void divY(Vector4d self, double y) { self.y = OperationHelper.div(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void divZ(Vector4d self, double z) { self.z = OperationHelper.div(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void divW(Vector4d self, double w) { self.w = OperationHelper.div(self.x, w); }

    @LuaExpose @LuaReturnSelf public static void mod(Vector4d self, Vector4d other) {
        self.set(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y), OperationHelper.mod(self.z, other.z), OperationHelper.mod(self.w, other.w));
    }
    @LuaExpose @LuaReturnSelf public static void mod(Vector4d self, double x, double y, double z, double w) {
        self.set(OperationHelper.mod(self.x, x), OperationHelper.mod(self.y, y), OperationHelper.mod(self.z, z), OperationHelper.mod(self.w, w));
    }
    @LuaExpose @LuaReturnSelf public static void modX(Vector4d self, double x) { self.x = OperationHelper.mod(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void modY(Vector4d self, double y) { self.y = OperationHelper.mod(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void modZ(Vector4d self, double z) { self.z = OperationHelper.mod(self.x, z); }
    @LuaExpose @LuaReturnSelf public static void modW(Vector4d self, double w) { self.w = OperationHelper.mod(self.x, w); }

    @LuaExpose @LuaReturnSelf public static void normalize(Vector4d self) { self.normalize(); }
    @LuaExpose @LuaReturnSelf public static void clamp(Vector4d self, double min) {
        double len2 = self.lengthSquared();
        if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void clamp(Vector4d self, double min, double max) {
        double len2 = self.lengthSquared();
        if (len2 > max*max) self.normalize(max);
        else if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void floor(Vector4d self) { self.floor(); }
    @LuaExpose @LuaReturnSelf public static void ceil(Vector4d self) { self.ceil(); }
    @LuaExpose @LuaReturnSelf public static void round(Vector4d self) { self.round(); }

    @LuaExpose @LuaReturnSelf public static void maxWith(Vector4d self, Vector4d other) { self.max(other); }
    @LuaExpose @LuaReturnSelf public static void maxWith(Vector4d self, double x, double y, double z, double w) { self.set(Math.max(self.x, x),Math.max(self.y, y),Math.max(self.z, z),Math.max(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void minWith(Vector4d self, Vector4d other) { self.min(other); }
    @LuaExpose @LuaReturnSelf public static void minWith(Vector4d self, double x, double y, double z, double w) { self.set(Math.min(self.x, x),Math.min(self.y, y),Math.min(self.z, z),Math.min(self.w, w)); }
    @LuaExpose @LuaReturnSelf public static void lerp(Vector4d self, Vector4d other, double amount) { self.lerp(other, amount); }

    // Non-mutating operations, returning a different type
    @LuaExpose public static double dot(Vector4d self, Vector4d other) { return self.dot(other); }
    @LuaExpose public static double dot(Vector4d self, double x, double y, double z, double w) { return self.dot(x, y, z, w); }
    @LuaExpose public static Vector4d copy(Vector4d self) { return new Vector4d(self); }
    @LuaExpose public static double len(Vector4d self) { return self.length(); }
    @LuaExpose public static double len2(Vector4d self) { return self.lengthSquared(); }
    @LuaExpose public static Varargs unpack(Vector4d self) { return ValueFactory.varargsOf(LuaDouble.valueOf(self.x), LuaDouble.valueOf(self.y), LuaDouble.valueOf(self.z), LuaDouble.valueOf(self.w)); }
    @LuaExpose public static void abs(Vector4d self) { self.absolute(); }
    @LuaExpose public static double max(Vector4d self) { return self.x >= self.y && self.x >= self.z && self.x >= self.w ? self.x : (self.y >= self.z && self.y >= self.w ? self.y : Math.max(self.z, self.w)); }
    @LuaExpose public static double min(Vector4d self) { return self.x <= self.y && self.x <= self.z && self.x <= self.w ? self.x : (self.y <= self.z && self.y <= self.w ? self.y : Math.min(self.z, self.w)); }

    // Binary operator overloading.
    // Needs extra logic since we want to allow Vector + Vector and Vector + number.
    @LuaExpose @LuaPassState public static Vector4d __add(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return new Vector4d(self).add(other);
        double num = unknown.checkDouble(s);
        return new Vector4d(self).add(num, num, num, num);
    }
    @LuaExpose @LuaPassState public static Vector4d __sub(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return new Vector4d(self).sub(other);
        double num = unknown.checkDouble(s);
        return new Vector4d(self).sub(num, num, num, num);
    }
    @LuaExpose @LuaPassState public static Vector4d __mul(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return new Vector4d(self).mul(other);
        double num = unknown.checkDouble(s);
        return new Vector4d(self).mul(num);
    }
    @LuaExpose @LuaPassState public static Vector4d __div(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return new Vector4d(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y), OperationHelper.div(self.z, other.z), OperationHelper.div(self.w, other.w));
        double num = unknown.checkDouble(s);
        return new Vector4d(OperationHelper.div(self.x, num), OperationHelper.div(self.y, num), OperationHelper.div(self.z, num), OperationHelper.div(self.w, num));
    }
    @LuaExpose @LuaPassState public static Vector4d __mod(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return new Vector4d(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y), OperationHelper.mod(self.z, other.z), OperationHelper.mod(self.w, other.w));
        double num = unknown.checkDouble(s);
        return new Vector4d(OperationHelper.mod(self.x, num), OperationHelper.mod(self.y, num), OperationHelper.mod(self.z, num), OperationHelper.mod(self.w, num));
    }
    @LuaExpose @LuaPassState public static boolean __eq(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return self.equals(other);
        double num = unknown.checkDouble(s);
        return self.equals(num, num, num, num);
    }
    @LuaExpose @LuaPassState public static boolean __lt(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return self.x < other.x && self.y < other.y && self.z < other.z && self.w < other.w;
        double num = unknown.checkDouble(s);
        return self.x < num && self.y < num && self.z < num && self.w < num;
    }
    @LuaExpose @LuaPassState public static boolean __le(LuaRuntime s, Vector4d self, LuaValue unknown) throws LuaError, LuaUncatchableError {
        Vector4d other = unknown.optUserdata(s, Vector4d.class, null);
        if (other != null) return self.x <= other.x && self.y <= other.y && self.z <= other.z && self.w <= other.w;
        double num = unknown.checkDouble(s);
        return self.x <= num && self.y <= num && self.z <= num && self.w <= num;
    }

    // Unary ops
    @LuaExpose public static Vector4d __unm(Vector4d self) { return self.negate(new Vector4d()); }
    @LuaExpose public static double __len(Vector4d self) { return self.length(); }

    // Swizzling! :D

    @LuaExpose @LuaPassState public static LuaValue __index(LuaRuntime s, Vector4d self, LuaValue key) throws LuaError, LuaUncatchableError {
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

    @LuaExpose @LuaPassState public static void __newindex(LuaRuntime s, Vector4d self, LuaValue key, LuaValue value) throws LuaError, LuaUncatchableError {
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
    private static void setSwizzle(Vector4d v, char c, double num, LuaState state) throws LuaError, LuaUncatchableError {
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
