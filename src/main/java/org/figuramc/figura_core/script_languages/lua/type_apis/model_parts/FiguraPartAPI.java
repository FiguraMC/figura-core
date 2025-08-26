package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.parts.FigmodelModelPart;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.part.tasks.TextTask;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.text.FormattedText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@LuaTypeAPI(typeName = "FiguraPart", wrappedClass = FiguraModelPart.class, hasSuperclass = true)
public class FiguraPartAPI {

    public static LuaUserdata wrap(FiguraModelPart part, LuaRuntime state) {
        return switch (part) {
            case FigmodelModelPart figmodel -> FigmodelAPI.wrap(figmodel, state);
            default -> new LuaUserdata(part, state.figuraMetatables.figuraPart);
        };
    }

    // Create new FiguraPart, optionally giving it a name
    @LuaExpose(name = "new") @LuaPassState
    public static FiguraModelPart _new(LuaRuntime s) throws LuaUncatchableError {
        try {
            return new FiguraModelPart("", List.of(), s.avatar.allocationTracker);
        } catch (AvatarError err) { throw new LuaUncatchableError(err); }
    }
    @LuaExpose(name = "new") @LuaPassState
    public static FiguraModelPart _new(LuaRuntime s, LuaString name) throws LuaUncatchableError {
        try {
            return new FiguraModelPart(name.toJavaString(s.allocationTracker), List.of(), s.avatar.allocationTracker);
        } catch (AvatarError err) { throw new LuaUncatchableError(err); }
    }

    // Add/remove children from this part
    @LuaExpose @LuaReturnSelf
    public static void addChild(FiguraModelPart self, FiguraModelPart newChild) throws LuaUncatchableError {
        try { self.addChild(newChild); }
        catch (AvatarError e) { throw new LuaUncatchableError(e); }
    }
    @LuaExpose @LuaReturnSelf
    public static void removeChild(FiguraModelPart self, FiguraModelPart childToRemove) {
        self.removeChild(childToRemove);
    }

    // Get a child by name, or nil if it doesn't exist
    @LuaExpose
    public static @Nullable FiguraModelPart child(FiguraModelPart self, LuaString childName) {
        return self.getChildByName(childName.toJavaStringNoAlloc());
    }

    // Get a list-like table of all children
    @LuaExpose @LuaPassState
    public static LuaTable children(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaUncatchableError {
        LuaTable result = new LuaTable(self.children.size(), 0, s.allocationTracker);
        int i = 1;
        for (FiguraModelPart child : self.children)
            result.rawset(i++, wrap(child, s));
        return result;
    }

    // Create a new text task on this part with the given Text object and return it
    @LuaExpose @LuaPassState
    public static TextTask newText(LuaRuntime s, FiguraModelPart self, FormattedText text) throws LuaUncatchableError {
        try {
            TextTask task = new TextTask(text, s.avatar.allocationTracker);
            self.renderTasks.add(task);
            return task;
        }
        catch (AvatarError avatarError) { throw new LuaUncatchableError(avatarError); }
    }

    // Make a shallow copy of this part, with no parent. We use "shallow" to make it clear what's going on.
    // Acts as a sensible default for :copy(nil, true, false, false, false)
    @LuaExpose @LuaPassState
    public static FiguraModelPart shallowCopy(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaUncatchableError {
        return copy(s, self, Constants.NIL, true, false, false, false);
    }

    // Acts as a sometimes-sensible default for :copy(nil, true, true, true, true)
    @LuaExpose @LuaPassState
    public static FiguraModelPart deepCopy(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaUncatchableError {
        return copy(s, self, Constants.NIL, true, true, true, true);
    }

    @LuaExpose @LuaPassState
    public static FiguraModelPart copy(
            LuaRuntime s, FiguraModelPart self,
            LuaValue newName,
            boolean deepCopyTransform,
            boolean deepCopyChildren,
            boolean deepCopyVertices,
            boolean deepCopyCallbackLists
    ) throws LuaError, LuaUncatchableError {
        try {
            String name = newName.optString(s, self.name);
            return new FiguraModelPart(name, self, deepCopyTransform, deepCopyChildren, deepCopyVertices, deepCopyCallbackLists, s.avatar.allocationTracker);
        } catch (AvatarError e) {
            throw new LuaUncatchableError(e); // Wrap avatar errors :P
        }
    }

    // Custom __index which fetches a child by name, or errors if it doesn't exist.
    @LuaExpose @LuaPassState
    public static @NotNull FiguraModelPart __index(LuaRuntime s, FiguraModelPart self, LuaString key) throws LuaError, LuaUncatchableError {
        @Nullable FiguraModelPart maybeChild = child(self, key);
        if (maybeChild == null) throw new LuaError("Model part \"" + key + "\" does not exist.", s.allocationTracker);
        return maybeChild;
    }

}
