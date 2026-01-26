package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.text.FormattedText;

import java.util.UUID;

// Define printing functions in globals

public class FiguraPrint {

    public static void init(LuaRuntime state) throws LuaOOM {

        state.globals().rawset("printToLog", LibFunction.create((s, arg) -> {
            String message = arg.toJavaString(state.allocationTracker);
            Object avatarKey = ((LuaRuntime) s).avatar.key;
            FiguraConnectionPoint.CONSOLE_OUTPUT.logVerbose(avatarKey, message);
            return Constants.NIL;
        }));
        state.globals().rawset("printNative", LibFunction.create((s, arg) -> {
            String message = arg.toJavaString(state.allocationTracker);
            FiguraConnectionPoint.CONSOLE_OUTPUT.logNativeFormatted(message);
            return Constants.NIL;
        }));
        state.globals().rawset("printFormatted", LibFunction.create((s, arg) -> {
            FormattedText formattedText = arg.checkUserdata(s, FormattedText.class);
            FiguraConnectionPoint.CONSOLE_OUTPUT.logFormatted(formattedText);
            return Constants.NIL;
        }));
        state.globals().rawset("print", LibFunction.createV((s, args) -> {
            StringBuilder sb = new StringBuilder();
            int n = args.count();
            for (int i = 1; i <= n; i++) {
                sb.append(args.arg(i).toJavaString(state.allocationTracker));
                if (i < n) sb.append("    ");
            }
            String message = sb.toString();
            if (state.allocationTracker != null) {
                state.allocationTracker.track(message);
            }
            Object avatarKey = ((LuaRuntime) s).avatar.key;
            FiguraConnectionPoint.CONSOLE_OUTPUT.logSimple(avatarKey, message);
            return Constants.NIL;
        }));


    }

}
