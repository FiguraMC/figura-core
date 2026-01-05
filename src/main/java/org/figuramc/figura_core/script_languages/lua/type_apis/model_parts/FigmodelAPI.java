package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.animation.AnimationInstance;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.parts.FigmodelModelPart;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;

@LuaTypeAPI(typeName = "Figmodel", wrappedClass = FigmodelModelPart.class, hasSuperclass = true)
public class FigmodelAPI {

    public static LuaUserdata wrap(FigmodelModelPart modelPart, LuaRuntime state) {
        return new LuaUserdata(modelPart, state.figuraMetatables.figmodelModelPart);
    }

    @LuaExpose
    public static @Nullable AnimationInstance animation(FigmodelModelPart self, LuaString animName) {
        return self.animation(animName.toJavaString(null));
    }

}
