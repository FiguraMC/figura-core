package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Add this to indicate the LuaRuntime (extends LuaState) should be passed as the first arg.
 * Needed when operating on LuaValue.
 */
@Target(ElementType.METHOD)
public @interface LuaPassState {
}
