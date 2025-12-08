package org.figuramc.figura_core.script_languages.lua.type_apis.rendering;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

@LuaTypeAPI(typeName = "Texture", wrappedClass = AvatarTexture.class)
public class TextureAPI {

    public static LuaUserdata wrap(AvatarTexture texture, LuaRuntime state) {
        return new LuaUserdata(texture, state.figuraMetatables.texture);
    }



}
