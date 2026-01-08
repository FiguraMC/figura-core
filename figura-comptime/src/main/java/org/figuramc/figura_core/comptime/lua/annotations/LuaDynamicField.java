package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Target a method with this to turn that method into a dynamic field.
 * It should be a method with the only arg being the self param,
 * as well as the LuaRuntime if using @LuaPassState as well.
 *
 * This will cause an error if your class also has a custom __index.
 * (TODO: Allow these to work together? Only if we need it)
 */
@Target(ElementType.METHOD)
public @interface LuaDynamicField {}
