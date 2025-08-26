package org.figuramc.figura_core.script_languages.lua.other_apis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.text.TextStyle;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.jetbrains.annotations.Nullable;

public class TextTable {

    public static LuaTable create(LuaRuntime state, @Nullable Molang molang) throws LuaError, LuaUncatchableError {
        LuaTable text = new LuaTable(state.allocationTracker);

        // text.raw(string): Creates a raw text, with no formatting, holding the given string
        text.rawset("raw", LibFunction.create((s, string) -> new LuaUserdata(new FormattedText(string.checkString(s)))));

        // text.json(json string): Create a formatted text using the given JSON string
        text.rawset("json", LibFunction.create((s, string) -> {
            try {
                JsonElement json = new Gson().fromJson(string.checkString(s), JsonElement.class);
                return new LuaUserdata(new FormattedText(json, TextStyle.NO_STYLE, molang));
            } catch (AvatarError oom) {
                throw new LuaUncatchableError(oom);
            } catch (JsonSyntaxException invalidJson) {
                throw new LuaError("Invalid JSON: " + invalidJson.getMessage(), s.allocationTracker);
            } catch (MolangCompileException ex) {
                throw new LuaError(ex.getMessage(), s.allocationTracker);
            }
        }));

        return text;
    }

}
