package org.figuramc.figura_core.script_languages.lua;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.cc.tweaked.cobalt.internal.unwind.SuspendedAction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.compiler.CompileException;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.compiler.LoadState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.Dispatch;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaClosure;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.interrupt.InterruptAction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.lib.Bit32Lib;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.lib.CoreLibraries;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.ScriptRuntimeComponent;
import org.figuramc.figura_core.avatars.components.EntityRoot;
import org.figuramc.figura_core.avatars.components.HudRoot;
import org.figuramc.figura_core.avatars.components.ManagerAccess;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.figuramc.figura_core.script_languages.lua.callback_types.convert.CallbackItemToLua;
import org.figuramc.figura_core.script_languages.lua.callback_types.convert.LuaToCallbackItem;
import org.figuramc.figura_core.script_languages.lua.other_apis.*;
import org.figuramc.figura_core.script_languages.lua.type_apis.model_parts.FiguraPartAPI;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.DelegateAllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;

public class LuaRuntime extends LuaState implements ScriptRuntimeComponent<LuaRuntime> {

    public static final Type<LuaRuntime> TYPE = new Type<>(LuaRuntime::create, VanillaRendering.TYPE, EntityRoot.TYPE, HudRoot.TYPE, ManagerAccess.TYPE);

    // Map each module index to its environment, so we can initialize them
    private final Map<Integer, LuaTable> moduleEnvironments = new IdentityHashMap<>();
    // Metatables for Figura types
    public final FiguraMetatables figuraMetatables;
    // Converters for callback types
    public final LuaToCallbackItem luaToCallbackItem;
    public final CallbackItemToLua callbackItemToLua;
    // The avatar this runtime is attached to. If an error occurs, this is who to blame.
    public Avatar<?> avatar; // Can also fetch its allocation tracker if needed!

    // Use this for the factory since it throws AvatarError
    public static LuaRuntime create(Avatar<?> avatar, AvatarModules modules) throws AvatarError {
        try {
            return new LuaRuntime(avatar, modules);
        } catch (LuaError luaError) {
            throw new AvatarError(FiguraException.INTERNAL_ERROR, new TranslatableItems.Items1<>("Failed to initialize Lua runtime"), luaError);
        } catch (LuaUncatchableError wrapper) {
            throw (AvatarError) wrapper.getCause();
        }
    }

    /**
     * Create a Lua runtime, but do not run any user code yet!
     */
    private LuaRuntime(Avatar<?> avatar, AvatarModules modules) throws LuaError, LuaUncatchableError, AvatarError {
        // Initialize as LuaState
        super(LuaState.builder()
                .interruptHandler(() -> InterruptAction.CONTINUE)
                .allocationTracker(avatar.allocationTracker == null ? null : new DelegateAllocationTracker<>(avatar.allocationTracker, AvatarError.class, LuaUncatchableError::new))
        );
        // Save avatar, so we know who to blame if an error occurs
        this.avatar = avatar;

        // Fetch other components we depend on
        @Nullable VanillaRendering vanillaRendering = avatar.getComponent(VanillaRendering.TYPE);
        @Nullable EntityRoot entityRoot = avatar.getComponent(EntityRoot.TYPE);
        @Nullable HudRoot hudRoot = avatar.getComponent(HudRoot.TYPE);
        @Nullable ManagerAccess managerAccess = avatar.getComponent(ManagerAccess.TYPE);


        // Add type metatables. They're shared across modules, in the true globals
        figuraMetatables = new FiguraMetatables(this);
        figuraMetatables.addTypesTo(globals());

        luaToCallbackItem = new LuaToCallbackItem(this);
        callbackItemToLua = new CallbackItemToLua(this);

        // Add global APIs
        CoreLibraries.standardGlobals(this);
        Bit32Lib.add(this);
        FiguraMath.init(this);
        globals().rawset("events", EventsTable.create(this, avatar.eventListeners));
        globals().rawset("client", ClientTable.create(this));

        // Add tables for other components we have
        if (vanillaRendering != null) globals().rawset("vanilla", VanillaTable.create(this, vanillaRendering));
        if (managerAccess != null) globals().rawset("manager", ManagerTable.create(this, managerAccess));

        // Set up a separate env for each lua module:
        for (AvatarModules.LoadTimeModule module : modules.loadTimeModules()) {
            // Ensure this module uses this runtime before setting it up here
            if (module.runtimeType != TYPE) continue;
            // Create an _ENV for this module with its stuff
            LuaTable _ENV = new LuaTable(this.allocationTracker);
            // Set _ENV's metatable to have __index = globals, so things in state.globals() are shared across allModules
            _ENV.setMetatable(this, ValueFactory.tableOf(this.allocationTracker, Constants.INDEX, globals()));
            // Save the environment so we can initialize
            moduleEnvironments.put(module.index, _ENV);
            // Create globals unique to this module:

            // models:
            LuaTable models = ValueFactory.tableOf(this.allocationTracker);
            _ENV.rawset("models", models);
            if (entityRoot != null && entityRoot.rootByModule[module.index] != null) models.rawset("entity", FiguraPartAPI.wrap(entityRoot.rootByModule[module.index], this));
            if (hudRoot != null && hudRoot.rootByModule[module.index] != null) models.rawset("hud", FiguraPartAPI.wrap(hudRoot.rootByModule[module.index], this));

            // Create require() for this module
            _ENV.rawset("require", FiguraRequire.createRequire(this, _ENV, module));
        }
    }

    @Override
    public void initModule(AvatarModules.RuntimeModule module) throws AvatarError {
        try {
            LuaTable env = moduleEnvironments.get(module.index);
            if (env == null) throw new AvatarError(FiguraException.INTERNAL_ERROR, new TranslatableItems.Items1<>("Attempt to initialize non-Lua module with Lua runtime?"));
            // Compile and run entrypoint, which is just "require 'main'"
            LuaClosure entrypoint = LoadState.load(this, new ByteArrayInputStream("return require 'main'".getBytes(StandardCharsets.UTF_8)), "=ENTRYPOINT", env);
            LuaValue res = this.runNoYield(entrypoint, Constants.NONE).first();
            // Fetch resulting table, and generate script callbacks for the module
            for (var entry : module.api.entrySet()) {
                String funcName = entry.getKey();
                CallbackType.Func<?, ?> funcType = entry.getValue();
                LuaTable table = res.checkTable(this, "main.lua is expected to return a table to implement module's API");
                LuaFunction func = table.rawget(funcName).checkFunction(this, "Expected main.lua to provide a function for key \"" + funcName + "\", but got " + table.rawget(funcName).typeName());
                LuaCallback<?, ?> callback = new LuaCallback<>(funcType, this, func);
                module.callbacks.put(funcName, callback);
            }
        } catch (LuaError luaError) {
            throw new AvatarError(FiguraException.LITERAL, new TranslatableItems.Items1<>(luaError.getMessage().replace("\t", "  ")), luaError);
        } catch (CompileException impossible) {
            throw new AvatarError(FiguraException.INTERNAL_ERROR, new TranslatableItems.Items1<>("Failed to compile Lua entrypoint. Should be impossible. Bug in Figura, please report!"), impossible);
        } catch (LuaUncatchableError wrapper) {
            throw (AvatarError) wrapper.getCause();
        }
    }

    // Helper methods to run a file defined in org/figuramc/figura_core/scripts/lua/
    public static Varargs runAssetFile(LuaState state, String name) throws LuaError, LuaUncatchableError {
        return runAssetFile(state, name, Constants.NONE);
    }
    public static Varargs runAssetFile(LuaState state, String name, Varargs args) throws LuaError, LuaUncatchableError {
        try(InputStream input = LuaRuntime.class.getClassLoader().getResourceAsStream("org/figuramc/figura_core/scripts/lua/" + name + ".lua")) {
            // Compile the file
            if (input == null) throw new LuaError("Figura is missing the builtin file \"" + name + ".lua\"", state.allocationTracker);
            LuaClosure c = LoadState.load(state, input, "=" + name.toUpperCase(), state.globals());
            // Execute the file
            return state.runNoYield(c, args);
        } catch (IOException e) {
            throw new LuaError("Figura is missing the builtin file \"" + name + ".lua\"", state.allocationTracker);
        } catch (CompileException e) {
            throw new LuaError("Figura failed to compile builtin file \"" + name + ".lua\"", state.allocationTracker);
        }
    }


}
