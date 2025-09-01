package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDirect;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_languages.lua.LuaEscaper;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;

import java.util.Objects;

/**
 * Wraps a Callback created elsewhere, passed **TO LUA**
 */
@LuaTypeAPI(typeName = "Callback", wrappedClass = ScriptCallback.class)
public class CallbackAPI {

    public static LuaUserdata wrap(ScriptCallback<?, ?> callback, LuaRuntime state) {
        return new LuaUserdata(callback, state.figuraMetatables.callback);
    }

    @LuaExpose
    public static CallbackType<?> type(ScriptCallback<?, ?> callback) {
        return callback.type();
    }

    @LuaExpose @LuaDirect
    public static Varargs __call(LuaState s, Varargs args) throws LuaError, LuaUncatchableError {
        // Fetch callback from userdata
        ScriptCallback<?, ?> callback = args.first().checkUserdata(s, ScriptCallback.class);
        // Fast track. If it's a LuaCallback and it uses the same LuaState, we can go faster with localCall.
        Varargs result;
        if (callback instanceof LuaCallback<?,?> luaCallback && luaCallback.state == s) {
            result = luaCallback.localCall(args.subargs(2));
        } else {
            result = callImpl((LuaRuntime) s, callback, args.subargs(2));
        }
        // If our avatar is errored, throw an escaper so we can get out of Lua
        if (((LuaRuntime) s).avatar.isErrored()) throw LuaEscaper.INSTANCE;
        return result;
    }

    // Separate into its own method so we can use generics properly
    private static <I extends CallbackItem, O extends CallbackItem> Varargs callImpl(LuaRuntime s, ScriptCallback<I, O> callback, Varargs args) throws LuaError, LuaUncatchableError {
        // Handle tuple args specially. We treat tuples as lua varargs at top level for convenience
        CallbackType.Func<I, O> ty = callback.type();
        // Typecheck (and count-check) the provided args against the expected args
        int requiredArgCount = ty.param() instanceof CallbackType.Tuple<?> tuple ? tuple.count() : 1;
        if (args.count() != requiredArgCount)
            throw new LuaError("Attempt to call callback with incorrect number of args. Expected " + requiredArgCount + ", got " + args.count(), s.allocationTracker);
        I input = ty.param() instanceof CallbackType.Tuple<I> tuple ? tuple.toItems(s.luaToCallbackItem, args.toArray()) : ty.param().toItem(s.luaToCallbackItem, args.first());
        // Invoke the function
        O output = callback.call(input);
        // Convert results back into Lua and return
        return ty.returnType() instanceof CallbackType.Tuple<O> tuple ? ValueFactory.varargsOf(tuple.fromItems(s.callbackItemToLua, output, LuaValue[]::new)) : ty.returnType().fromItem(s.callbackItemToLua, output);
    }

}
