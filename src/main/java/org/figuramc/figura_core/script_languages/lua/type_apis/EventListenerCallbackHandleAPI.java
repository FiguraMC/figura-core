package org.figuramc.figura_core.script_languages.lua.type_apis;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

@LuaTypeAPI(typeName = "EventListenerCallbackHandle", wrappedClass = EventListener.CallbackHandle.class)
public class EventListenerCallbackHandleAPI {

    public static LuaUserdata wrap(EventListener.CallbackHandle handle, LuaRuntime state) {
        return new LuaUserdata(handle, state.figuraMetatables.eventListenerCallbackHandle);
    }

    @LuaExpose @LuaPassState
    public static void remove(LuaRuntime s, EventListener.CallbackHandle self) {
        if (!EventListenerAPI.isOwningAvatar(s, self.listener)) return;
        self.removeThis();
    }

}
