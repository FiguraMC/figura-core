package org.figuramc.figura_core.script_languages.lua.type_apis.model_parts;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.comptime.lua.annotations.LuaExpose;
import org.figuramc.figura_core.comptime.lua.annotations.LuaPassState;
import org.figuramc.figura_core.comptime.lua.annotations.LuaReturnSelf;
import org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@LuaTypeAPI(typeName = "VanillaPart", wrappedClass = VanillaRendering.ScriptVanillaPart.class, hasSuperclass = true)
public class VanillaPartAPI {

    public static LuaUserdata wrap(VanillaRendering.ScriptVanillaPart part, LuaRuntime state) {
        return new LuaUserdata(part, state.figuraMetatables.vanillaPart);
    }

    // Getter/setter for canceling vanilla operations
    @LuaExpose public static boolean cancelOrigin(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaOrigin; }
    @LuaExpose @LuaReturnSelf public static void cancelOrigin(VanillaRendering.ScriptVanillaPart self, boolean cancel) { self.cancelVanillaOrigin = cancel; }
    @LuaExpose public static boolean cancelRot(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaRotation; }
    @LuaExpose @LuaReturnSelf public static void cancelRot(VanillaRendering.ScriptVanillaPart self, boolean cancel) { self.cancelVanillaRotation = cancel; }
    @LuaExpose public static boolean cancelScale(VanillaRendering.ScriptVanillaPart self) { return self.cancelVanillaScale; }
    @LuaExpose @LuaReturnSelf public static void cancelScale(VanillaRendering.ScriptVanillaPart self, boolean cancel) { self.cancelVanillaScale = cancel; }

    // Fetching stored values
    @LuaExpose public static Vector3d storedOrigin(VanillaRendering.ScriptVanillaPart self) { return self.storedVanillaOrigin.get(new Vector3d()); }
    @LuaExpose public static Vector3d storedRot(VanillaRendering.ScriptVanillaPart self) { return storedRad(self).mul(180 / Math.PI); }
    @LuaExpose public static Vector3d storedRad(VanillaRendering.ScriptVanillaPart self) { return self.storedVanillaRotation.get(new Vector3d()); }
    @LuaExpose public static Vector3d storedScale(VanillaRendering.ScriptVanillaPart self) { return self.storedVanillaScale.get(new Vector3d()); }
    @LuaExpose public static Vector3d storedPos(VanillaRendering.ScriptVanillaPart self) { return self.storedVanillaPosition.get(new Vector3d()); }

    // Children
    @LuaExpose @LuaPassState public static LuaTable children(LuaRuntime s, VanillaRendering.ScriptVanillaPart self) throws LuaError, LuaUncatchableError {
        LuaTable result = new LuaTable(s.allocationTracker);
        for (var entry : self.children.entrySet()) {
            result.rawset(entry.getKey(), wrap(entry.getValue(), s));
        }
        return result;
    }

    // Custom __index fetches a child
    @LuaExpose public static @Nullable VanillaRendering.ScriptVanillaPart __index(VanillaRendering.ScriptVanillaPart self, LuaString key) {
        return self.getChildByName(key.toJavaStringNoAlloc());
    }

}
