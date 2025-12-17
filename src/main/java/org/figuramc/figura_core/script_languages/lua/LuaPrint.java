package org.figuramc.figura_core.script_languages.lua;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;

import java.util.UUID;

public final class LuaPrint {

    public LuaPrint(Avatar<?> owner) {
        printToLog = LibFunction.create((state, arg) -> {
            Object k = owner.key;
            UUID ownerID = k instanceof UUID uuid ? uuid : null;
            FiguraConnectionPoint.CONSOLE_OUTPUT.logVerbose(ownerID, arg.toJavaString(state.allocationTracker));
            return Constants.NIL;
        });
        printNative = LibFunction.create((state, arg) -> {
            String nativeContent = arg.toJavaString(state.allocationTracker);
            FiguraConnectionPoint.CONSOLE_OUTPUT.logNativeFormatted(nativeContent);
            return Constants.NIL;
        });
        print = LibFunction.createV((state, arg) -> {
            StringBuilder sb = new StringBuilder();
            int n = arg.count();
            for (int i = 1; i <= n; i++) {
                sb.append(arg.arg(i).toJavaString(state.allocationTracker));
                if (i < n) sb.append("    ");
            }
            String result = sb.toString();
            if (state.allocationTracker != null) {
                state.allocationTracker.track(result);
            }
            Object k = owner.key;
            UUID ownerID = k instanceof UUID uuid ? uuid : null;
            FiguraConnectionPoint.CONSOLE_OUTPUT.logSimple(ownerID, result);
            return Constants.NIL;
        });
    }

    public final LuaFunction printNative;
    public final LuaFunction printToLog;
    public final LuaFunction print;
}
