package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.model_parts.VanillaPartAPI;

/**
 * The global table "vanilla", storing APIs related to rendering of the vanilla entity.
 */
public class VanillaTable {

    public static LuaTable create(LuaRuntime state, VanillaRendering component) throws LuaError, LuaOOM {
        LuaTable vanilla = new LuaTable(state.allocationTracker);

        // Simple getter/setter pairs:

        // vanilla.hideAllParts(). 0 arg getter, 1 arg setter.
        vanilla.rawset("hideAllParts", LibFunction.createV((s, args) -> {
            switch (args.count()) {
                case 0 -> { return LuaBoolean.valueOf(component.hideAllModelParts); }
                case 1 -> component.hideAllModelParts = args.first().checkBoolean(s);
                default -> throw ErrorFactory.argCountError(s, "vanilla.hideAllParts()", args.count(), 0, 1);
            }
            return Constants.NONE;
        }));

        // vanilla.model is a table of all model parts by name
        LuaTable model = ValueFactory.tableOf(state.allocationTracker);
        for (var entry : component.nameToScriptPart.entrySet())
            model.rawset(entry.getKey(), VanillaPartAPI.wrap(entry.getValue(), state));
        vanilla.rawset("model", model);

        return vanilla;
    }

}
