package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates a function that should run directly.
 * Should accept a LuaState (not LuaRuntime) and Varargs, and return Varargs.
 * It cannot have overloads.
 * It's passed directly as a method reference to LibFunction.createV(),
 */
@Target(ElementType.METHOD)
public @interface LuaDirect {}
