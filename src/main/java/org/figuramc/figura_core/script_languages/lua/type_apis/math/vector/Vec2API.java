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

@LuaTypeAPI(typeName = "Vec2", wrappedClass = Vector2d.class)
public class Vec2API {

    public static LuaUserdata wrap(Vector2d vec, LuaRuntime state) throws LuaOOM {
        if (state.allocationTracker != null)
            state.allocationTracker.track(vec, AllocationTracker.OBJECT_SIZE + AllocationTracker.DOUBLE_SIZE * 2);
        return new LuaUserdata(vec, state.figuraMetatables.vec2);
    }

    // Mutating operations
    @LuaExpose @LuaReturnSelf public static void set(Vector2d self) { self.zero(); }
    @LuaExpose @LuaReturnSelf public static void set(Vector2d self, Vector2d other) { self.set(other); }
    @LuaExpose @LuaReturnSelf public static void set(Vector2d self, double x, double y) { self.set(x, y); }
    @LuaExpose @LuaReturnSelf public static void setX(Vector2d self, double x) { self.x = x; }
    @LuaExpose @LuaReturnSelf public static void setY(Vector2d self, double y) { self.y = y; }
    @LuaExpose @LuaReturnSelf public static void setAll(Vector2d self, double v) { self.set(v); }

    @LuaExpose @LuaReturnSelf public static void add(Vector2d self, Vector2d other) { self.add(other); }
    @LuaExpose @LuaReturnSelf public static void add(Vector2d self, double x, double y) { self.add(x, y); }
    @LuaExpose @LuaReturnSelf public static void addX(Vector2d self, double x) { self.x += x; }
    @LuaExpose @LuaReturnSelf public static void addY(Vector2d self, double y) { self.y += y; }
    @LuaExpose @LuaReturnSelf public static void addAll(Vector2d self, double v) { self.add(v, v); }

    @LuaExpose @LuaReturnSelf public static void sub(Vector2d self, Vector2d other) { self.sub(other); }
    @LuaExpose @LuaReturnSelf public static void sub(Vector2d self, double x, double y) { self.sub(x, y); }
    @LuaExpose @LuaReturnSelf public static void subX(Vector2d self, double x) { self.x -= x; }
    @LuaExpose @LuaReturnSelf public static void subY(Vector2d self, double y) { self.y -= y; }
    @LuaExpose @LuaReturnSelf public static void subAll(Vector2d self, double v) { self.sub(v, v); }

    @LuaExpose @LuaReturnSelf public static void mul(Vector2d self, Vector2d other) { self.mul(other); }
    @LuaExpose @LuaReturnSelf public static void mul(Vector2d self, double x, double y) { self.x *= x; self.y *= y; }
    @LuaExpose @LuaReturnSelf public static void mulX(Vector2d self, double x) { self.x *= x; }
    @LuaExpose @LuaReturnSelf public static void mulY(Vector2d self, double y) { self.y *= y; }
    @LuaExpose @LuaReturnSelf public static void mulAll(Vector2d self, double v) { self.mul(v); }

    @LuaExpose @LuaReturnSelf public static void div(Vector2d self, Vector2d other) { self.set(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y)); }
    @LuaExpose @LuaReturnSelf public static void div(Vector2d self, double x, double y) { self.set(OperationHelper.div(self.x, x), OperationHelper.div(self.y, y)); }
    @LuaExpose @LuaReturnSelf public static void divX(Vector2d self, double x) { self.x = OperationHelper.div(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void divY(Vector2d self, double y) { self.y = OperationHelper.div(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void divAll(Vector2d self, double v) { div(self, v, v); }

    @LuaExpose @LuaReturnSelf public static void mod(Vector2d self, Vector2d other) { self.set(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y)); }
    @LuaExpose @LuaReturnSelf public static void mod(Vector2d self, double x, double y) { self.set(OperationHelper.mod(self.x, x), OperationHelper.mod(self.y, y)); }
    @LuaExpose @LuaReturnSelf public static void modX(Vector2d self, double x) { self.x = OperationHelper.mod(self.x, x); }
    @LuaExpose @LuaReturnSelf public static void modY(Vector2d self, double y) { self.y = OperationHelper.mod(self.x, y); }
    @LuaExpose @LuaReturnSelf public static void modAll(Vector2d self, double v) { mod(self, v, v); }

    @LuaExpose @LuaReturnSelf public static void max(Vector2d self, Vector2d other) { self.max(other); }
    @LuaExpose @LuaReturnSelf public static void max(Vector2d self, double x, double y) { self.set(Math.max(self.x, x),Math.max(self.y, y)); }
    @LuaExpose @LuaReturnSelf public static void maxX(Vector2d self, double x) { if (self.x < x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void maxY(Vector2d self, double y) { if (self.y < y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void maxAll(Vector2d self, double v) { max(self, v, v); }

    @LuaExpose @LuaReturnSelf public static void min(Vector2d self, Vector2d other) { self.min(other); }
    @LuaExpose @LuaReturnSelf public static void min(Vector2d self, double x, double y) { self.set(Math.min(self.x, x),Math.min(self.y, y)); }
    @LuaExpose @LuaReturnSelf public static void minX(Vector2d self, double x) { if (self.x > x) self.x = x; }
    @LuaExpose @LuaReturnSelf public static void minY(Vector2d self, double y) { if (self.y > y) self.y = y; }
    @LuaExpose @LuaReturnSelf public static void minAll(Vector2d self, double v) { min(self, v, v); }

    // We'll say result is undefined if min > max
    @LuaExpose @LuaReturnSelf public static void clamp(Vector2d self, Vector2d min, Vector2d max) { self.max(min); self.min(max); }
    @LuaExpose @LuaReturnSelf public static void clamp(Vector2d self, double x1, double y1, double x2, double y2) { self.set(Math.clamp(self.x, x1, x2), Math.clamp(self.y, y1, y2)); }
    @LuaExpose @LuaReturnSelf public static void clampX(Vector2d self, double x1, double x2) { if (self.x < x1) self.x = x1; else if (self.x > x2) self.x = x2; }
    @LuaExpose @LuaReturnSelf public static void clampY(Vector2d self, double y1, double y2) { if (self.y < y1) self.y = y1; else if (self.y > y2) self.y = y2; }
    @LuaExpose @LuaReturnSelf public static void clampAll(Vector2d self, double v1, double v2) { clamp(self, v1, v1, v2, v2); }

    @LuaExpose @LuaReturnSelf public static void neg(Vector2d self) { self.negate(); }
    @LuaExpose @LuaReturnSelf public static void negX(Vector2d self) { self.x = -self.x; }
    @LuaExpose @LuaReturnSelf public static void negY(Vector2d self) { self.y = -self.y; }

    @LuaExpose @LuaReturnSelf public static void abs(Vector2d self) { self.absolute(); }
    @LuaExpose @LuaReturnSelf public static void absX(Vector2d self) { self.x = Math.abs(self.x); }
    @LuaExpose @LuaReturnSelf public static void absY(Vector2d self) { self.y = Math.abs(self.y); }

    @LuaExpose @LuaReturnSelf public static void deg(Vector2d self) { self.mul(180 / Math.PI); }
    @LuaExpose @LuaReturnSelf public static void rad(Vector2d self) { self.mul(Math.PI / 180); }

    @LuaExpose @LuaReturnSelf public static void normalize(Vector2d self) { self.normalize(); }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector2d self, double min) {
        double len2 = self.lengthSquared();
        if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void clampLength(Vector2d self, double min, double max) {
        double len2 = self.lengthSquared();
        if (len2 > max*max) self.normalize(max);
        else if (len2 < min*min) self.normalize(min);
    }
    @LuaExpose @LuaReturnSelf public static void floor(Vector2d self) { self.floor(); }
    @LuaExpose @LuaReturnSelf public static void ceil(Vector2d self) { self.ceil(); }
    @LuaExpose @LuaReturnSelf public static void round(Vector2d self) { self.round(); }
    @LuaExpose @LuaReturnSelf public static void lerp(Vector2d self, Vector2d other, double amount) { self.lerp(other, amount); }

    // Non-mutating operations or operations returning a different type
    @LuaExpose public static double dot(Vector2d self, Vector2d other) { return self.dot(other); }
    @LuaExpose public static double dot(Vector2d self, double x, double y) { return self.x * x + self.y * y; }
    @LuaExpose public static Vector2d copy(Vector2d self) { return new Vector2d(self); }
    @LuaExpose public static double len(Vector2d self) { return self.length(); }
    @LuaExpose public static double len2(Vector2d self) { return self.lengthSquared(); }
    @LuaExpose public static Varargs unpack(Vector2d self) { return ValueFactory.varargsOf(LuaDouble.valueOf(self.x), LuaDouble.valueOf(self.y)); }
    @LuaExpose public static double maxElem(Vector2d self) { return Math.max(self.x, self.y); }
    @LuaExpose public static double minElem(Vector2d self) { return Math.min(self.x, self.y); }

    // Binary operator overloading.
    // Needs extra logic since we want to allow Vector + Vector and Vector + number.
    @LuaExpose @LuaPassState public static Vector2d __add(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return new Vector2d(self).add(other);
        double num = unknown.checkDouble(s);
        return new Vector2d(self).add(num, num);
    }
    @LuaExpose @LuaPassState public static Vector2d __sub(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return new Vector2d(self).sub(other);
        double num = unknown.checkDouble(s);
        return new Vector2d(self).sub(num, num);
    }
    @LuaExpose @LuaPassState public static Vector2d __mul(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return new Vector2d(self).mul(other);
        double num = unknown.checkDouble(s);
        return new Vector2d(self).mul(num);
    }
    @LuaExpose @LuaPassState public static Vector2d __div(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return new Vector2d(OperationHelper.div(self.x, other.x), OperationHelper.div(self.y, other.y));
        double num = unknown.checkDouble(s);
        return new Vector2d(OperationHelper.div(self.x, num), OperationHelper.div(self.y, num));
    }
    @LuaExpose @LuaPassState public static Vector2d __mod(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return new Vector2d(OperationHelper.mod(self.x, other.x), OperationHelper.mod(self.y, other.y));
        double num = unknown.checkDouble(s);
        return new Vector2d(OperationHelper.mod(self.x, num), OperationHelper.mod(self.y, num));
    }
    @LuaExpose @LuaPassState public static boolean __eq(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return self.equals(other);
        double num = unknown.checkDouble(s);
        return self.equals(num, num);
    }
    @LuaExpose @LuaPassState public static boolean __lt(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return self.x < other.x && self.y < other.y;
        double num = unknown.checkDouble(s);
        return self.x < num && self.y < num;
    }
    @LuaExpose @LuaPassState public static boolean __le(LuaRuntime s, Vector2d self, LuaValue unknown) throws LuaError, LuaOOM {
        if (unknown instanceof LuaUserdata userdata && userdata.userdata() instanceof Vector2d other)
            return self.x <= other.x && self.y <= other.y;
        double num = unknown.checkDouble(s);
        return self.x <= num && self.y <= num;
    }

    // Unary ops
    @LuaExpose public static Vector2d __unm(Vector2d self) { return self.negate(new Vector2d()); }
    @LuaExpose public static double __len(Vector2d self) { return self.length(); }

    // Swizzling! :D

    @LuaExpose @LuaPassState public static LuaValue __index(LuaRuntime s, Vector2d self, LuaValue key) throws LuaError, LuaOOM {
        // Numeric key, index it like an array
        if (key.type() == TNUMBER) {
            return switch (key.toInteger()) {
                case 1 -> LuaDouble.valueOf(self.x);
                case 2 -> LuaDouble.valueOf(self.y);
                default -> NIL;
            };
        }
        // Key should be a string then
        LuaString k = key.checkLuaString(s);
        // Get the swizzle going
        try {
            return switch (k.length()) {
                case 1 -> k.charAt(0) == 'c' ? Vec2API.wrap(new Vector2d(self), s) : LuaDouble.valueOf(getSwizzle(self, (char) k.charAt(0)));
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

    @LuaExpose @LuaPassState public static void __newindex(LuaRuntime s, Vector2d self, LuaValue key, LuaValue value) throws LuaError, LuaOOM {
        // Numeric key, index like an array
        if (key.type() == TNUMBER) {
            switch (key.toInteger()) {
                case 1 -> self.x = value.checkDouble(s);
                case 2 -> self.y = value.checkDouble(s);
                default -> throw ErrorFactory.argError(s, key, "integer 1 to 2");
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

    private static double getSwizzle(Vector2d v, char c) throws InvalidSwizzleException {
        return switch (c) {
            case '_' -> 0;
            case 'i' -> 1;
            case 'I' -> -1;
            case 'x', 'r', 'u' -> v.x;
            case 'y', 'g', 'v' -> v.y;
            case 'X', 'R', 'U' -> -v.x;
            case 'Y', 'G', 'V' -> -v.y;
            default -> throw InvalidSwizzleException.INSTANCE; // Signal to caller to return nil
        };
    }
    private static void setSwizzle(Vector2d v, char c, double num, LuaState state) throws LuaError, LuaOOM {
        switch (c) {
            case '_' -> {}
            case 'x', 'r', 'u' -> v.x = num;
            case 'y', 'g', 'v' -> v.y = num;
            case 'X', 'R', 'U' -> v.x = -num;
            case 'Y', 'G', 'V' -> v.y = -num;
            default -> throw new LuaError("Invalid swizzle character to Vec2: '" + c + "'", state.allocationTracker); // Swizzle assignment should error on invalid
        }
    }

    private static class InvalidSwizzleException extends Exception {
        private static final InvalidSwizzleException INSTANCE = new InvalidSwizzleException();
    }

}
