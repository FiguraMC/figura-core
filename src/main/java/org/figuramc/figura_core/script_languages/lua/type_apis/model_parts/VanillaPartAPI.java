package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.comptime.lua.annotations.*;
import org.figuramc.figura_core.script_hooks.flags.QueuedSetters;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@LuaTypeAPI(typeName = "VanillaPart", wrappedClass = VanillaRendering.ScriptVanillaPart.class, hasSuperclass = true)
public class VanillaPartAPI {

    public static LuaUserdata wrap(VanillaRendering.ScriptVanillaPart part, LuaRuntime state) {
        return new LuaUserdata(part, state.figuraMetatables.vanillaPart);
    }

    // Getter/setter for canceling vanilla operations.
    // Setters are queued as normal
    @LuaExpose public static boolean cancelOrigin(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaOrigin; }
    @LuaExpose @LuaReturnSelf public static void cancelOrigin(VanillaRendering.ScriptVanillaPart self, boolean cancel) { QueuedSetters.handle(() -> self.cancelVanillaOrigin = cancel); }
    @LuaExpose public static boolean cancelRot(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaRotation; }
    @LuaExpose @LuaReturnSelf public static void cancelRot(VanillaRendering.ScriptVanillaPart self, boolean cancel) { QueuedSetters.handle(() -> self.cancelVanillaRotation = cancel); }
    @LuaExpose public static boolean cancelScale(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaScale; }
    @LuaExpose @LuaReturnSelf public static void cancelScale(VanillaRendering.ScriptVanillaPart self, boolean cancel) { QueuedSetters.handle(() -> self.cancelVanillaScale = cancel); }

    // Getters for fetching stored values (only consistent in render thread context)

    @LuaExpose @LuaDynamicField public static Vector3d storedOrigin(VanillaRendering.ScriptVanillaPart self) { return self.storedOrigin.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d fullStoredOrigin(VanillaRendering.ScriptVanillaPart self) { return self.fullStoredOrigin.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d storedRot(VanillaRendering.ScriptVanillaPart self) { return storedRad(self).mul(180 / Math.PI); }
    @LuaExpose @LuaDynamicField public static Vector3d fullStoredRot(VanillaRendering.ScriptVanillaPart self) { return fullStoredRad(self).mul(180 / Math.PI); }
    @LuaExpose @LuaDynamicField public static Vector3d storedRad(VanillaRendering.ScriptVanillaPart self) { return self.storedRotation.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d fullStoredRad(VanillaRendering.ScriptVanillaPart self) { return self.fullStoredRotation.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d storedScale(VanillaRendering.ScriptVanillaPart self) { return self.storedScale.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d fullStoredScale(VanillaRendering.ScriptVanillaPart self) { return self.fullStoredScale.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d storedPos(VanillaRendering.ScriptVanillaPart self) { return self.storedPosition.get(new Vector3d()); }
    @LuaExpose @LuaDynamicField public static Vector3d fullStoredPos(VanillaRendering.ScriptVanillaPart self) { return self.fullStoredPosition.get(new Vector3d()); }

    // Child getter; since children can't be modified we don't need to worry about consistency with this one
    @LuaExpose @LuaPassState public static LuaTable children(LuaRuntime s, VanillaRendering.ScriptVanillaPart self) throws LuaError, LuaOOM {
        LuaTable result = new LuaTable(s.allocationTracker);
        for (var entry : self.children.entrySet()) {
            result.rawset(entry.getKey(), wrap(entry.getValue(), s));
        }
        return result;
    }

    // Custom __index fetches a child
    @LuaExpose @LuaPassState public static @Nullable VanillaRendering.ScriptVanillaPart __index(LuaRuntime s, VanillaRendering.ScriptVanillaPart self, LuaValue key) throws LuaError, LuaOOM {
        return self.getChildByName(key.checkString(s));
    }

}
