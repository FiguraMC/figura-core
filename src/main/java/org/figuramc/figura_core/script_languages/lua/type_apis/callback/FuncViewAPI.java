package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDirect;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.FuncView;
import org.figuramc.figura_core.script_languages.lua.LuaEscaper;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a Callback created elsewhere, passed **TO LUA**
 */
@LuaTypeAPI(typeName = "FuncView", wrappedClass = FuncView.class)
public class FuncViewAPI {

    public static LuaUserdata wrap(FuncView<?, ?> view, LuaRuntime state) {
        return new LuaUserdata(view, state.figuraMetatables.funcView);
    }

    @LuaExpose(name = "new") @LuaPassState
    public static FuncView<?,?> _new(LuaRuntime s, LuaValue func, CallbackType<?> paramType, CallbackType<?> returnType) {
        return new FuncView<>(new LuaCallback<>(new CallbackType.Func<>(paramType, returnType), s, func));
    }

    @LuaExpose public static void revoke(FuncView<?, ?> self) { self.close(); }
    @LuaExpose public static boolean isRevoked(FuncView<?, ?> self) { return self.isRevoked(); }

    // Attempt to call the function. If the view has been revoked, instead does nothing and returns nothing.
    @LuaExpose @LuaDirect
    public static Varargs __call(LuaState s, Varargs args) throws LuaError, LuaUncatchableError {
        // Fetch callback from userdata
        FuncView<?, ?> funcView = args.first().checkUserdata(s, FuncView.class);
        ScriptCallback<?,?> callback = funcView.getValue();
        if (callback == null) { return Constants.NONE; } // If it's been revoked, return nothing.

        // Fast track. If it's a LuaCallback and it uses the same LuaState, we can go faster with localCall.
        Varargs result;
        if (callback instanceof LuaCallback<?,?> luaCallback && luaCallback.state == s) {
            result = luaCallback.localCall(args.subargs(2));
        } else {
            result = callImpl((LuaRuntime) s, callback, args.subargs(2));
            // If the callee errored, we will return None, same as if the view was revoked.
            if (result == null) result = Constants.NONE;
        }
        // If our own avatar is errored after this, throw an escaper so we can get out of Lua
        if (((LuaRuntime) s).avatar.isErrored()) throw LuaEscaper.INSTANCE;
        return result;
    }

    // Separate into its own method so we can use generics properly
    private static <I extends CallbackItem, O extends CallbackItem> @Nullable Varargs callImpl(LuaRuntime s, ScriptCallback<I, O> callback, Varargs args) throws LuaError, LuaUncatchableError {
        // Handle tuple args specially. We treat tuples as lua varargs at top level for convenience
        CallbackType.Func<I, O> ty = callback.type();
        // Typecheck (and count-check) the provided args against the expected args
        int requiredArgCount = ty.param() instanceof CallbackType.Tuple<?> tuple ? tuple.count() : 1;
        if (args.count() != requiredArgCount)
            throw new LuaError("Attempt to call callback with incorrect number of args. Expected " + requiredArgCount + ", got " + args.count(), s.allocationTracker);
        I input = ty.param() instanceof CallbackType.Tuple<I> tuple ? tuple.toItems(s.luaToCallbackItem, args.toArray()) : ty.param().toItem(s.luaToCallbackItem, args.first());
        // Invoke the function
        O output = callback.call(input);
        if (output == null) return null; // The callee errored, so let's return null out of here.
        // Convert results back into Lua and return
        return ty.returnType() instanceof CallbackType.Tuple<O> tuple ? ValueFactory.varargsOf(tuple.fromItems(s.callbackItemToLua, output)) : ty.returnType().fromItem(s.callbackItemToLua, output);
    }

}
