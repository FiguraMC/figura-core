package org.figuramc.figura_core.comptime.lua.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotate a class with this to mark it as a Lua Type API class.
 * This class should have static methods annotated with @LuaExpose.
 *
 * It also must have a static method wrap(), accepting wrappedClass and LuaState, and returning a LuaUserdata.
 */
@Target(ElementType.TYPE)
public @interface LuaTypeAPI {
    String typeName();
    Class<?> wrappedClass();
    boolean hasSuperclass() default false; // If it has a superclass, adds a parameter to the createMetatable method to pass it.
}
