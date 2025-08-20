package org.figuramc.figura_core.script_languages.lua.type_apis;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDirect;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;

@LuaTypeAPI(typeName = "EventListener", wrappedClass = EventListener.class)
public class EventListenerAPI {

    public static LuaUserdata wrap(EventListener<?> eventListener, LuaRuntime state) {
        return new LuaUserdata(eventListener, state.figuraMetatables.eventListener);
    }

    @LuaExpose @LuaPassState public static void register(LuaRuntime s, EventListener<?> self, LuaValue arg) {
        registerImpl(s, self, arg);
    }

    @LuaExpose @LuaDirect public static Varargs __call(LuaState s, Varargs args) throws LuaError, LuaUncatchableError {
        EventListener<?> self = args.first().checkUserdata(s, EventListener.class);
        callImpl((LuaRuntime) s, self, args.subargs(2));
        return Constants.NONE;
    }

    // Helper method to let generics work
    public static <T extends CallbackItem> void registerImpl(LuaRuntime state, EventListener<T> listener, LuaValue func) {
        listener.registerCallback(new LuaCallback<>(listener.funcType, state, func));
    }

    // Separate into its own method so we can use generics properly
    private static <I extends CallbackItem> void callImpl(LuaRuntime s, EventListener<I> eventListener, Varargs args) throws LuaError, LuaUncatchableError {
        // Handle tuple args specially. We treat tuples as lua varargs at top level for convenience
        CallbackType<I> paramType = eventListener.funcType.param();
        // Typecheck (and count-check) the provided args against the expected args
        int requiredArgCount = paramType instanceof CallbackType.Tuple<?> tuple ? tuple.count() : 1;
        if (args.count() != requiredArgCount)
            throw new LuaError("Attempt to call callback with incorrect number of args. Expected " + requiredArgCount + ", got " + args.count(), s.allocationTracker);
        I input = paramType instanceof CallbackType.Tuple<I> tuple ? tuple.toItems(s.luaToCallbackItem, args.toArray()) : paramType.toItem(s.luaToCallbackItem, args.first());
        // Invoke the function
        eventListener.invoke(input);
    }

}
