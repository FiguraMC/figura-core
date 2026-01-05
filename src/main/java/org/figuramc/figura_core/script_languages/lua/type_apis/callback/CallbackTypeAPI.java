package org.figuramc.figura_core.script_languages.lua.type_apis.callback;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue;
import org.figuramc.figura_core.comptime.lua.annotations.LuaConstant;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;

@LuaTypeAPI(typeName = "CallbackType", wrappedClass = CallbackType.class)
public class CallbackTypeAPI {

    // TODO make this use memory
    public static LuaUserdata wrap(CallbackType<?> callbackType, LuaRuntime state) {
        return new LuaUserdata(callbackType, state.figuraMetatables.callbackType);
    }

    // Non-generic types are constants
    @LuaConstant public static LuaValue UNIT(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.Unit.INSTANCE, metatable); }
    @LuaConstant public static LuaValue OPAQUE(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.Opaque.INSTANCE, metatable); }
    @LuaConstant public static LuaValue ANY(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.Any.INSTANCE, metatable); }
    @LuaConstant public static LuaValue BOOL(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.Bool.INSTANCE, metatable); }
    @LuaConstant public static LuaValue F32(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.F32.INSTANCE, metatable); }
    @LuaConstant public static LuaValue F64(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.F64.INSTANCE, metatable); }
    @LuaConstant public static LuaValue STRING(LuaState state, LuaTable metatable) { return new LuaUserdata(CallbackType.Str.INSTANCE, metatable); }

    // Generics are functions over types
    @LuaExpose public static CallbackType<?> List(CallbackType<?> elemType) { return new CallbackType.List<>(elemType); }
    @LuaExpose public static CallbackType<?> Map(CallbackType<?> keyType, CallbackType<?> valueType) { return new CallbackType.Map<>(keyType, valueType); }
    @LuaExpose public static CallbackType<?> Func(CallbackType<?> inputType, CallbackType<?> outputType) { return new CallbackType.Func<>(inputType, outputType); }
    @LuaExpose public static CallbackType<?> Optional(CallbackType<?> innerType) { return new CallbackType.Optional<>(innerType); }

    // TODO Tuples

}
