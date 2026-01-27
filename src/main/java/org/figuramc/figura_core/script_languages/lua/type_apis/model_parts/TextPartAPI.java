package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.parts.TextModelPart;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

@LuaTypeAPI(typeName = "TextPart", wrappedClass = TextModelPart.class, hasSuperclass = true)
public class TextPartAPI {

    public static LuaUserdata wrap(TextModelPart modelPart, LuaRuntime state) {
        return new LuaUserdata(modelPart, state.figuraMetatables.textPart);
    }

}
