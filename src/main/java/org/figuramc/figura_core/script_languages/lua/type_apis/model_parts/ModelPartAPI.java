package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.model.part.parts.FigmodelModelPart;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.part.parts.TextModelPart;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.errors.LuaAvatarError;
import org.figuramc.figura_core.text.FormattedText;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@LuaTypeAPI(typeName = "ModelPart", wrappedClass = FiguraModelPart.class, hasSuperclass = true)
public class ModelPartAPI {

    public static LuaUserdata wrap(FiguraModelPart part, LuaRuntime state) {
        return switch (part) {
            case FigmodelModelPart figmodel -> FigmodelAPI.wrap(figmodel, state);
            case TextModelPart textPart -> TextPartAPI.wrap(textPart, state);
            default -> new LuaUserdata(part, state.figuraMetatables.modelPart);
        };
    }

    // -------- CREATION -------- //

    // Create new empty FiguraPart, optionally giving it a name
    @LuaExpose(name = "new") @LuaPassState
    public static FiguraModelPart _new(LuaRuntime s) throws LuaOOM {
        try {
            return new FiguraModelPart(s.avatar, "", List.of());
        } catch (AvatarOutOfMemoryError err) { throw new LuaOOM(err); }
    }
    @LuaExpose(name = "new") @LuaPassState
    public static FiguraModelPart _new(LuaRuntime s, LuaString name) throws LuaOOM {
        try {
            return new FiguraModelPart(s.avatar, name.toJavaString(s.allocationTracker), List.of());
        } catch (AvatarOutOfMemoryError err) { throw new LuaOOM(err); }
    }

    // Create a new TextModelPart with the given text and add it as a child of this one. Return the new part
    @LuaExpose @LuaPassState
    public static TextModelPart addText(LuaRuntime s, FiguraModelPart self, String basicText) throws LuaOOM {
        return addFormattedText(s, self, new FormattedText(basicText));
    }

    @LuaExpose @LuaPassState
    public static TextModelPart addFormattedText(LuaRuntime s, FiguraModelPart self, FormattedText text) throws LuaOOM {
        try {
            TextModelPart part = new TextModelPart(s.avatar, "", text);
            self.addChild(part);
            return part;
        } catch (AvatarOutOfMemoryError err) { throw new LuaOOM(err); }
    }

//    // Make a shallow copy of this part, with no parent. We use "shallow" to make it clear what's going on.
//    // Acts as a sensible default for :copy(nil, true, false, false, false)
//    @LuaExpose @LuaPassState
//    public static FiguraModelPart shallowCopy(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaOOM {
//        return copy(s, self, Constants.NIL, true, false, false, false);
//    }
//
//    // Acts as a sometimes-sensible default for :copy(nil, true, true, true, true)
//    @LuaExpose @LuaPassState
//    public static FiguraModelPart deepCopy(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaOOM {
//        return copy(s, self, Constants.NIL, true, true, true, true);
//    }
//
//    @LuaExpose @LuaPassState
//    public static FiguraModelPart copy(
//            LuaRuntime s, FiguraModelPart self,
//            LuaValue newName,
//            boolean deepCopyTransform,
//            boolean deepCopyChildren,
//            boolean deepCopyVertices,
//            boolean deepCopyCallbackLists
//    ) throws LuaError, LuaOOM {
//        try {
//            String name = newName.optString(s, self.name);
//            return new FiguraModelPart(name, self, deepCopyTransform, deepCopyChildren, deepCopyVertices, deepCopyCallbackLists, s.avatar.allocationTracker);
//        } catch (AvatarOutOfMemoryError e) {
//            throw new LuaOOM(e); // Wrap avatar errors :P
//        }
//    }

    // -------- RENDERING -------- //

    // Get/set material and material priority
    @LuaExpose @LuaReturnSelf
    public static @Nullable FiguraRenderType material(FiguraModelPart self) {
        // Might be null if materials were merged to the parent!
        // This getter is very rarely what you want... TODO consider should we even have it?
        return self.renderType;
    }
    // TODO add a way to remove material/set to null
    @LuaExpose @LuaReturnSelf
    public static void material(FiguraModelPart self, FiguraRenderType material, int priority) {
        QueuedSetters.handle(() -> { self.renderType = material; self.renderTypePriority = priority; });
    }
    @LuaExpose @LuaReturnSelf
    public static void material(FiguraModelPart self, FiguraRenderType material) {
        QueuedSetters.handle(() -> { self.renderType = material; });
    }

    // Rebuild/build vertex data! Could be expensive. Be careful when using this.
    // TODO: Could we add some more safety to this somehow? So you can't forget to adhere to the rules:
    //       - IF you changed the model part tree structure, remember to rebuild() any ancestor parts which already had rendering data
    //       - Is that really it?
    @LuaExpose @LuaReturnSelf
    public static void rebuild(FiguraModelPart self) throws LuaAvatarError, LuaOOM {
        try {
            self.buildRenderingData();
        } catch (AvatarError err) {
            throw new LuaAvatarError(err);
        } catch (AvatarOutOfMemoryError err) {
            throw new LuaOOM(err);
        }
    }


    // -------- CHILDREN / STRUCTURE -------- //

    // Add child
    @LuaExpose @LuaReturnSelf
    public static void addChild(FiguraModelPart self, FiguraModelPart newChild) throws LuaOOM {
        try { self.addChild(newChild); }
        catch (AvatarOutOfMemoryError e) { throw new LuaOOM(e); }
    }

    // Remove child
    @LuaExpose @LuaReturnSelf
    public static void removeChild(FiguraModelPart self, FiguraModelPart childToRemove) {
        self.removeChild(childToRemove);
    }

    // Get a child by name, or nil if it doesn't exist
    @LuaExpose
    public static @Nullable FiguraModelPart child(FiguraModelPart self, LuaString childName) {
        return self.getChildByName(childName.toJavaString(null));
    }

    // Get a list-like table of all children
    @LuaExpose @LuaPassState
    public static LuaTable children(LuaRuntime s, FiguraModelPart self) throws LuaError, LuaOOM {
        return s.listToTable(self.children, (r, child) -> wrap(child, r));
    }

    // Custom __index which fetches a child by name, or errors if it doesn't exist.
    // We use __preindex so this runs before looking for a method, so if a new method is added with the same name as a child, the avatar won't break.
    @LuaExpose @LuaPassState
    public static @Nullable FiguraModelPart __preindex(LuaRuntime s, FiguraModelPart self, LuaString key) throws LuaError, LuaOOM {
        return child(self, key.optLuaString(s, Constants.EMPTYSTRING));
    }
    // If we reach this custom __index, it means __preindex didn't succeed, and there is no method with this name either.
    // So it'll error unconditionally.
    @LuaExpose @LuaPassState
    public static LuaValue __index(LuaRuntime s, FiguraModelPart self, LuaValue key) throws LuaError, LuaOOM {
        throw new LuaError("There is no child or method with the name \"" + key + "\".", s.allocationTracker);
    }

}
