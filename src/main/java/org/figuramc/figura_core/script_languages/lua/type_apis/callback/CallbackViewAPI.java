package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.cc.tweaked.cobalt.internal.unwind.AutoUnwind;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.debug.DebugFrame;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.comptime.lua.annotations.*;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackView;
import org.figuramc.figura_core.script_hooks.callback.items.StringView;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.figuramc.figura_core.script_languages.lua.errors.LuaAvatarError;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a Callback created (possibly) elsewhere, passed **TO LUA**
 * Note that this type SHOULD NOT be actually used over avatar boundaries, only within the same Avatar.
 * Attempting to invoke one that's defined in another Avatar will return nothing and do nothing.
 */
@LuaTypeAPI(typeName = "CallbackView", wrappedClass = CallbackView.class)
public class CallbackViewAPI {

    public static LuaUserdata wrap(CallbackView<?, ?> callbackView, LuaRuntime state) {
        return new LuaUserdata(callbackView, state.figuraMetatables.callbackView);
    }

    @LuaExpose(name = "new") @LuaPassState
    public static CallbackView<?,?> _new(LuaRuntime s, LuaValue func, CallbackType<?> paramType, CallbackType<?> returnType) {
        return new CallbackView<>(new LuaCallback<>(new CallbackType.Func<>(paramType, returnType), s, func));
    }

    @LuaExpose public static void revoke(StringView self) { self.close(); }
    @LuaExpose public static boolean isRevoked(StringView self) { return self.isRevoked(); }
    @LuaExpose public static int length(StringView self) { return self.length(); }

    // Attempt to call the function.
    // If the function was defined in a different avatar, does nothing.
    @LuaExpose @LuaDirect
    public static Varargs __call(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        // Fetch callback from userdata
        CallbackView<?, ?> callbackView = args.first().checkUserdata(s, CallbackView.class);
        ScriptCallback<?, ?> callback = callbackView.getValue();
        // If revoked, return nothing
        if (callback == null) return Constants.NONE;
        // If it's owned by a different avatar (Java-owned callbacks are okay), return nothing.
        if (callback.getOwningAvatar() != null && callback.getOwningAvatar() != s.avatar) {
            return Constants.NONE;
        }
        // Fast track. If it's a LuaCallback (and it uses the same LuaState (this should be implicit, since it's the same avatar, but check anyway)), we can go faster with localCall.
        Varargs result;
        if (callback instanceof LuaCallback<?,?> luaCallback && luaCallback.state == s) {
            result = luaCallback.localCall(args.subargs(2));
        } else {
            result = callImpl(s, callback, args.subargs(2));
        }
        return result;
    }

    // Separate into its own method so we can use generics properly
    private static <I extends CallbackItem, O extends CallbackItem> @Nullable Varargs callImpl(LuaRuntime s, ScriptCallback<I, O> callback, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        // Handle tuple args specially. We treat tuples as lua varargs at top level for convenience
        CallbackType.Func<I, O> ty = callback.type();
        // Typecheck (and count-check) the provided args against the expected args
        int requiredArgCount = ty.param() instanceof CallbackType.Tuple<?> tuple ? tuple.count() : 1;
        if (args.count() != requiredArgCount)
            throw new LuaError("Attempt to call callback with incorrect number of args. Expected " + requiredArgCount + ", got " + args.count(), s.allocationTracker);
        I input = ty.param() instanceof CallbackType.Tuple<I> tuple ? tuple.toItems(s.luaToCallbackItem, args.toArray()) : ty.param().toItem(s.luaToCallbackItem, args.first());
        // Invoke the function, wrapping errors into LuaUncatchableError
        try {
            O output = callback.call(input);
            // Convert results back into Lua and return
            return ty.returnType() instanceof CallbackType.Tuple<O> tuple ? ValueFactory.varargsOf(tuple.fromItems(s.callbackItemToLua, output)) : ty.returnType().fromItem(s.callbackItemToLua, output);
        } catch (AvatarError avatarError) {
            throw new LuaAvatarError(avatarError);
        }
    }

}
