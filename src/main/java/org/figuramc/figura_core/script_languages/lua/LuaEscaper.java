package org.figuramc.figura_core.script_languages.lua;

import org.figuramc.figura_cobalt.LuaUncatchableError;

public class LuaEscaper extends LuaUncatchableError {
    public static final LuaEscaper INSTANCE = new LuaEscaper();
    private LuaEscaper() {}
}
