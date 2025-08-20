package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.FigmodelModelPart;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@LuaTypeAPI(typeName = "FiguraPart", wrappedClass = FiguraModelPart.class, hasSuperclass = true)
public class FiguraPartAPI {

    public static LuaUserdata wrap(FiguraModelPart part, LuaRuntime state) {
        return switch (part) {
            case FigmodelModelPart figmodel -> FigmodelAPI.wrap(figmodel, state);
            default -> new LuaUserdata(part, state.figuraMetatables.modelPart);
        };
    }

    // Get child by name, or nil if it doesn't exist
    @LuaExpose
    public static @Nullable FiguraModelPart child(FiguraModelPart self, LuaString childName) {
        return self.getChildByName(childName.toJavaStringNoAlloc());
    }

    // Get a table of all children, indexed by name
    @LuaExpose @LuaPassState
    public static LuaTable children(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaUncatchableError {
        LuaTable result = new LuaTable(s.allocationTracker);
        for (var childEntry : self.children.entrySet())
            result.rawset(childEntry.getKey(), wrap(childEntry.getValue(), s));
        return result;
    }

    // Custom __index which fetches a child by name, or errors if it doesn't exist.
    @LuaExpose @LuaPassState
    public static @NotNull FiguraModelPart __index(LuaRuntime s, FiguraModelPart self, LuaString key) throws LuaError, LuaUncatchableError {
        @Nullable FiguraModelPart maybeChild = child(self, key);
        if (maybeChild == null) throw new LuaError("Model part \"" + key + "\" does not exist.", s.allocationTracker);
        return maybeChild;
    }

}
