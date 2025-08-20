package org.figuramc.figura_core.comptime.lua.annotations;

/**
 * Indicates a function that should run directly.
 * Should accept a LuaState (not LuaRuntime!) and Varargs, and return Varargs.
 * It cannot have overloads.
 */
public @interface LuaDirect {
}
