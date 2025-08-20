package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotate a method with this to make it only run once, and store the result in the metatable as a constant.
 * The method should accept a LuaState param and a LuaTable metatable, and should return a LuaValue.
 */
@Target(ElementType.METHOD)
public @interface LuaConstant {}
