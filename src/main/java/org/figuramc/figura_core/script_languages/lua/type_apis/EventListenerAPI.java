package org.figuramc.figura_core.script_languages.lua.type_apis;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.comptime.lua.annotations.LuaDirect;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.figuramc.figura_core.script_languages.lua.errors.LuaAvatarError;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;

import java.util.List;

/**
 * Event listeners have an Owning Avatar.
 * Only some functions are callable by non-owning Avatars.
 * Check which kind you are before doing functionality.
 */
@LuaTypeAPI(typeName = "EventListener", wrappedClass = EventListener.class)
public class EventListenerAPI {

    public static LuaUserdata wrap(EventListener<?, ?> eventListener, LuaRuntime state) {
        return new LuaUserdata(eventListener, state.figuraMetatables.eventListener);
    }

    private static boolean isOwningAvatar(LuaRuntime s, EventListener<?, ?> listener) {
        return s.avatar == listener.owningAvatar;
    }

    // -------- LUA API -------- //

    @LuaExpose @LuaPassState
    public static long register(LuaRuntime s, EventListener<?, ?> self, LuaValue arg) {
        return registerImpl(s, self, arg);
    }
    @LuaExpose @LuaPassState
    public static void remove(LuaRuntime s, EventListener<?, ?> self, long callbackId) {
        removeImpl(s, self, callbackId);
    }

    // General call. Will use invoke() if owner, or queue() if non-owner. Generally what you want.
    @LuaExpose @LuaDirect
    public static Varargs __call(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        callImpl(s, args.first().checkUserdata(s, EventListener.class), args.subargs(2));
        return Constants.NONE;
    }

    // -------- IMMEDIATE CALLS -------- //

    // No returns
    @LuaExpose @LuaDirect
    public static Varargs invoke(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        var listener = args.first().checkUserdata(s, EventListener.class);
        if (!isOwningAvatar(s, listener)) return Constants.NONE;
        invokeImpl(s, listener, args.subargs(2));
        return Constants.NONE;
    }

    // Only works when the return type is a boolean
    // If a registered function returns true, cancels future functions
    // Returns true if any of the functions canceled, false otherwise
    @LuaExpose @LuaDirect
    public static Varargs invokeCanceling(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        // Default predicate, checking bool value
        var listener = args.first().checkUserdata(s, EventListener.class);
        if (!isOwningAvatar(s, listener)) return Constants.NONE;
        if (listener.funcType.returnType() != CallbackType.Bool.INSTANCE)
            throw new LuaError("EventListener:invokeCanceling() only works when output of the listener is a boolean", s.allocationTracker);
        return LuaBoolean.valueOf(invokeCancelingImpl(s, listener, args.subargs(2), CallbackItem.Bool::value));
    }

    // Return a List of results from the registered functions
    @LuaExpose @LuaDirect
    public static Varargs invokeToList(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        var listener = args.first().checkUserdata(s, EventListener.class);
        if (!isOwningAvatar(s, listener)) return Constants.NONE;
        return invokeToListImpl(s, listener, args.subargs(2));
    }

    // Chain the results along. Only works with EventListener<T, T> where input/output are the same type.
    @LuaExpose @LuaDirect
    public static Varargs invokeChained(LuaState state, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        LuaRuntime s = (LuaRuntime) state;
        // Default chainer, just using identity function
        var listener = args.first().checkUserdata(s, EventListener.class);
        if (!isOwningAvatar(s, listener)) return Constants.NONE;
        if (!listener.funcType.param().equals(listener.funcType.returnType()))
            throw new LuaError("EventListener:invokeChained() only works when input/output of the listener are the same type", s.allocationTracker);
        return invokeChainedImpl(s, listener, args.subargs(2), x -> x);
    }

    // TODO: invokeCanceling with a custom predicate
    // TODO: invokeChained with a custom chainer

    // -------- QUEUE OPERATION -------- //

    @LuaExpose
    public static boolean queueEmpty(EventListener<?, ?> eventListener) {
        return eventListener.isQueueEmpty();
    }

    @LuaExpose @LuaDirect
    public static Varargs queue(LuaState state, Varargs args) throws LuaError, LuaOOM {
        LuaRuntime s = (LuaRuntime) state;
        queueImpl(s, args.first().checkUserdata(s, EventListener.class), args.subargs(2));
        return Constants.NONE;
    }

    // Poll a single input. If the input is a tuple, it's received as varargs.
    @LuaExpose @LuaPassState
    public static Varargs poll(LuaRuntime s, EventListener<?, ?> listener) throws LuaError, LuaOOM {
        if (!isOwningAvatar(s, listener)) return Constants.NONE;
        return pollImpl(s, listener);
    }

    // Helper function to poll and automatically handle all messages in the queue
    @LuaExpose @LuaPassState
    public static void handleAll(LuaRuntime s, EventListener<?, ?> listener) throws LuaError, LuaOOM, LuaUncatchableError {
        if (!isOwningAvatar(s, listener)) return;
        handleAllImpl(s, listener);
    }

    // -------- HELPER METHODS FOR GENERIC SAFETY -------- //


    public static <T extends CallbackItem, R extends CallbackItem> long registerImpl(LuaRuntime s, EventListener<T, R> listener, LuaValue func) {
        if (!isOwningAvatar(s, listener)) return -1;
        return listener.registerCallback(new LuaCallback<>(listener.funcType, s, func));
    }

    public static <T extends CallbackItem, R extends CallbackItem> void removeImpl(LuaRuntime s, EventListener<T, R> listener, long callbackId) {
        if (!isOwningAvatar(s, listener)) return;
        listener.removeCallback(callbackId);
    }

    // Convert from Lua Varargs into I
    private static <I extends CallbackItem> I luaToArgs(LuaRuntime s, EventListener<I, ?> listener, Varargs args) throws LuaError, LuaOOM {
        // Handle tuple args specially. We treat tuples as lua varargs at top level for convenience
        CallbackType<I> paramType = listener.funcType.param();
        // Typecheck (and count-check) the provided args against the expected args
        int requiredArgCount = paramType instanceof CallbackType.Tuple<?> tuple ? tuple.count() : 1;
        if (args.count() != requiredArgCount)
            throw new LuaError("Attempt to call callback with incorrect number of args. Expected " + requiredArgCount + ", got " + args.count(), s.allocationTracker);
        if (paramType instanceof CallbackType.Tuple<I> tuple) {
            return tuple.toItems(s.luaToCallbackItem, args.toArray());
        } else {
            return paramType.toItem(s.luaToCallbackItem, args.first());
        }
    }

    // Convert from I into Lua Varargs
    private static <I extends CallbackItem> Varargs argsToLua(LuaRuntime s, EventListener<I, ?> listener, I input) throws LuaError, LuaOOM {
        // Handle tuples specially, treat them as lua varargs at top level for convenience
        CallbackType<I> paramType = listener.funcType.param();
        if (paramType instanceof CallbackType.Tuple<I> tuple) {
            List<LuaValue> unpacked = tuple.fromItems(s.callbackItemToLua, input);
            return ValueFactory.varargsOf(unpacked);
        } else {
            return paramType.fromItem(s.callbackItemToLua, input);
        }
    }

    // Call with no return values
    private static <I extends CallbackItem> void callImpl(LuaRuntime s, EventListener<I, ?> listener, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        I input = luaToArgs(s, listener, args);
        // Run immediately or queue, depending which kind we are
        if (isOwningAvatar(s, listener)) {
            try {
                listener.invoke(input);
            } catch (AvatarError avatarError) {
                throw new LuaAvatarError(avatarError);
            }
        } else {
            listener.queueInvocation(input, new AvatarView<>(s.avatar));
        }
    }

    // Call with no return values
    private static <I extends CallbackItem> void invokeImpl(LuaRuntime s, EventListener<I, ?> listener, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        I input = luaToArgs(s, listener, args);
        try {
            listener.invoke(input);
        } catch (AvatarError avatarError) {
            throw new LuaAvatarError(avatarError);
        }
    }

    private static <I extends CallbackItem, O extends CallbackItem> boolean invokeCancelingImpl(LuaRuntime s, EventListener<I, O> listener, Varargs args, BiThrowingFunction<O, Boolean, LuaError, LuaOOM> shouldCancel) throws LuaError, LuaOOM, LuaUncatchableError {
        I input = luaToArgs(s, listener, args);
        try {
            return listener.invokeCanceling(input, shouldCancel);
        } catch (AvatarError avatarError) {
            throw new LuaAvatarError(avatarError);
        }
    }


    // Call and receive a list of return values
    private static <I extends CallbackItem, O extends CallbackItem> Varargs invokeToListImpl(LuaRuntime s, EventListener<I, O> listener, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        I input = luaToArgs(s, listener, args);
        try {
            List<O> outputs = listener.invokeToList(input);
            return s.<O, LuaError, LuaOOM>listToTable(outputs, (s2, output) -> listener.funcType.returnType().fromItem(s2.callbackItemToLua, output));
        } catch (AvatarError avatarError) {
            throw new LuaAvatarError(avatarError);
        }
    }

    // Call with a chained return value
    private static <I extends CallbackItem, O extends CallbackItem> Varargs invokeChainedImpl(LuaRuntime s, EventListener<I, O> listener, Varargs args, BiThrowingFunction<O, I, LuaError, LuaOOM> chainer) throws LuaError, LuaOOM, LuaUncatchableError {
        I input = luaToArgs(s, listener, args);
        try {
            I result = listener.invokeChained(input, chainer);
            return argsToLua(s, listener, result);
        } catch (AvatarError avatarError) {
            throw new LuaAvatarError(avatarError);
        }
    }

    // Queue the args. Can be called by non-owners!
    private static <I extends CallbackItem, O extends CallbackItem> void queueImpl(LuaRuntime s, EventListener<I, O> listener, Varargs args) throws LuaError, LuaOOM {
        I input = luaToArgs(s, listener, args);
        listener.queueInvocation(input, new AvatarView<>(s.avatar));
    }

    // Poll a single argument and return it expanded.
    // TODO Append AvatarAPI instance for the one who queued it
    // TODO Append timestamp for when it was run (relative timestamp, like nanos/micros since it was queued?)
    private static <I extends CallbackItem, O extends CallbackItem> Varargs pollImpl(LuaRuntime s, EventListener<I, O> listener) throws LuaError, LuaOOM {
        EventListener.QueuedInvocation<I> queued = listener.pollInvocation();
        if (queued == null) return Constants.NONE;
        I args = queued.args();
        return argsToLua(s, listener, args);
    }

    private static <I extends CallbackItem, O extends CallbackItem> void handleAllImpl(LuaRuntime s, EventListener<I, O> listener) throws LuaError, LuaOOM, LuaUncatchableError {
        while (!listener.isQueueEmpty()) {
            // Fetch an invocation and convert to Lua args
            Varargs luaArgs = pollImpl(s, listener);
            // Call all the functions
            invokeImpl(s, listener, luaArgs);
        }
    }


}
