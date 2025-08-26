package org.figuramc.figura_core.script_languages.lua.type_apis;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.tasks.TextTask;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.text.FormattedText;

// Superclass Transformable
@LuaTypeAPI(typeName = "TextTask", wrappedClass = TextTask.class, hasSuperclass = true)
public class TextTaskAPI {

    public static LuaUserdata wrap(TextTask task, LuaRuntime state) {
        return new LuaUserdata(task, state.figuraMetatables.textTask);
    }

    // Getter/setter for text object of this task
    @LuaExpose @LuaPassState public static LuaUserdata text(LuaRuntime s, TextTask self) { return new LuaUserdata(self.formattedText); }
    @LuaExpose @LuaReturnSelf public static void text(TextTask self, FormattedText text) { self.formattedText = text; }

}
