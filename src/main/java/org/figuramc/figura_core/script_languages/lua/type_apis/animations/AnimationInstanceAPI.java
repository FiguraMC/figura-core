package org.figuramc.figura_core.script_languages.lua.type_apis.animations;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.animation.AnimationInstance;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

@LuaTypeAPI(typeName = "AnimationInstance", wrappedClass = AnimationInstance.class)
public class AnimationInstanceAPI {

    public static LuaUserdata wrap(AnimationInstance instance, LuaRuntime state) {
        return new LuaUserdata(instance, state.figuraMetatables.animationInstance);
    }

    // Queued setters, since these are read by rendering code
    // Likewise, getters are not necessarily consistent on non-render-thread contexts

    @LuaExpose public static double time(AnimationInstance instance) { return instance.getTime(); }
    @LuaExpose @LuaReturnSelf public static void time(AnimationInstance instance, float time) { QueuedSetters.handle(() -> instance.setTime(time)); }

    @LuaExpose public static double strength(AnimationInstance instance) { return instance.getStrength(); }
    @LuaExpose @LuaReturnSelf public static void strength(AnimationInstance instance, float strength) { QueuedSetters.handle(() -> instance.setStrength(strength)); }

}
