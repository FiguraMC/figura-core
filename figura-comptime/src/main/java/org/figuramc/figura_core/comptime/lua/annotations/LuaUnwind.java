package org.figuramc.figura_core.comptime.lua.annotations;

/**
 * TODO (I think this is bugged, it broke cryptically the one time I tried it. Save for later)
 *
 * Similar to LuaDirect, but for a function that can unwind.
 * The callee should also be annotated with @AutoUnwind.
 * Accept a LuaState (not LuaRuntime), DebugFrame, and Varargs, and return Varargs.
 * It cannot have overloads.
 * It's passed directly as a method reference to LibFunction.createS().
 */
@Deprecated // Don't use this yet, it seems broken
public @interface LuaUnwind {
}
