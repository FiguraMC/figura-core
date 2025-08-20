package org.figuramc.figura_core.comptime.lua.annotations;

/**
 * Add this to indicate the LuaRuntime (extends LuaState) should be passed as the first arg.
 * Needed when operating on LuaValue.
 */
public @interface LuaPassState {
}
