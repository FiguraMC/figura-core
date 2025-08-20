package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Expose the function, optionally with an overridden name.
 */
@Target(ElementType.METHOD)
public @interface LuaExpose {
    String name() default "";
}
