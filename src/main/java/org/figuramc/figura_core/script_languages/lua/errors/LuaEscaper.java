package org.figuramc.figura_core.script_languages.lua.errors;

import org.figuramc.figura_cobalt.LuaUncatchableError;

/**
 * Thrown when we want to escape Lua, because the Avatar has already errored out.
 * This should generally just bubble up from everywhere in Lua that it's thrown.
 */
public class LuaEscaper extends LuaUncatchableError {

    public static final LuaEscaper INSTANCE = new LuaEscaper();
    private LuaEscaper() {}

}
