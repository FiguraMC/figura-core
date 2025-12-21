package org.figuramc.figura_core.script_languages.lua.callback_types;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.Dispatch;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

// Implementation of ScriptCallback for Lua
// This represents a callback CREATED BY LUA and sent elsewhere.
public class LuaCallback<I extends CallbackItem, O extends CallbackItem> implements ScriptCallback<I, O> {

    private final CallbackType.Func<I, O> type;
    public final LuaRuntime state; // The state that created this callback
    private final LuaValue wrapped; // The object which will be called as the callback, generally a LuaFunction but anything with __call works

    public LuaCallback(CallbackType.Func<I, O> type, LuaRuntime state, LuaValue wrapped) {
        this.type = type;
        this.state = state;
        this.wrapped = wrapped;
    }

    @Override
    public CallbackType.Func<I, O> type() {
        return type;
    }

    @Override
    public AvatarView<?> getOwningAvatar() {
        return new AvatarView<>(state.avatar);
    }

    // Callback function %s expected to return %s, but it returned %s.
    private static final Translatable<TranslatableItems.Items3<String, String, String>> INCORRECT_RETURN
            = Translatable.create("figura_core.error.script.lua.incorrect_callback_return", String.class, String.class, String.class);

    // Callback function %s had an error: \n%s
    private static final Translatable<TranslatableItems.Items2<String, String>> ERROR_IN_CALLBACK
            = Translatable.create("figura_core.error.script.lua.error_in_callback", String.class, String.class);

    // TODO look into caching the result of conversion when multiple callbacks are run with the same args (for example, events)
    // Remember that the things running here are the fault of the CALLEE.
    // So if an error arises, we don't throw it back to the CALLER, we fault the Avatar who created this incorrect callback.
    @Override
    public O call(@Nullable ScriptCallback<?, ?> caller, long timeout, I arg) {
        // If we're errored, don't call the function
        if (!state.avatar.isErrored()) {
            try {
                // Convert the arg into Lua Varargs to pass to our function.
                // We'll treat funcs with tuple/unit args specially, and make it simple to call it from Lua with just multiple args, not needing to wrap in a table.
                // NOTE: Converting I into the lua args could theoretically OOM, but this isn't an issue since
                // the lua args themselves cannot be arbitrarily large, since they're just VIEWS of big data structures.
                // Worst case is a nested evil tuple, but this function specifically declared that it would accept a big nested evil tuple; so it's again the callee's problem.
                Varargs luaArgs = type.param() instanceof CallbackType.Tuple<I> tuple ? ValueFactory.varargsOf(tuple.fromItems(state.callbackItemToLua, arg)) : type.param().fromItem(state.callbackItemToLua, arg);

                // Run the function, getting a result:
                Varargs luaResult = state.runNoYield(wrapped, luaArgs);
                try {
                    // Attempt to convert the result back to an O, and return.
                    if (type.returnType() instanceof CallbackType.Tuple<O> tuple) {
                        // Pad array with nils
                        LuaValue[] arr = new LuaValue[tuple.count()];
                        for (int i = 0; i < arr.length; i++)
                            arr[i] = luaResult.arg(i + 1);
                        return tuple.toItems(state.luaToCallbackItem, arr);
                    } else {
                        return type.returnType().toItem(state.luaToCallbackItem, luaResult.first());
                    }
                } catch (LuaError luaError) {
                    // Lua returned an incorrect type from the callback? Let's error with an appropriate message.
                    String funcName = wrapped instanceof LuaFunction f ? '"' + f.debugName() + '"' : "<" + wrapped.typeName() + ">";
                    String expectedType = type.returnType().stringify();
                    String actualType = (luaResult.count() == 1 ? Stream.of(luaResult.first()) : Arrays.stream(luaResult.toArray())).map(LuaValue::typeName).toList().toString(); // (WIP. Could be better...)
                    state.avatar.error(new AvatarError(INCORRECT_RETURN, new TranslatableItems.Items3<>(funcName, expectedType, actualType)));
                }
            } catch (LuaError luaError) {
                // The callback encountered an error while running.
                String funcName = wrapped instanceof LuaFunction f ? '"' + f.debugName() + '"' : "<" + wrapped.typeName() + ">";
                state.avatar.error(new AvatarError(ERROR_IN_CALLBACK, new TranslatableItems.Items2<>(funcName, luaError.getMessage()), luaError));
            } catch (LuaUncatchableError wrapper) {
                // The callback's owner ran out of memory
                state.avatar.error((AvatarError) wrapper.getCause());
            }
        }
        // If the callee has errored, return null.
        return null;
    }

    // Do NOT invoke this across different LuaState! Ensure it's the same first.
    // This is here for the fast track of invoking callbacks from within the same LuaState,
    // meaning no conversions are needed.
    public Varargs localCall(Varargs args) throws LuaError, LuaUncatchableError {
        try {
            // TODO optionally type-check the args/return type for improved error messages, even if there's no conversions happening
            return Dispatch.invoke(state, wrapped, args);
        } catch (UnwindThrowable yielded) {
            throw new LuaError("Cannot yield() from within Figura callback!", state.allocationTracker);
        }
    }

}