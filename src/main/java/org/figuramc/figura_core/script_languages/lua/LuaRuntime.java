package org.figuramc.figura_core.script_languages.lua;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.compiler.CompileException;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.compiler.LoadState;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaClosure;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.interrupt.InterruptAction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.interrupt.InterruptHandler;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.lib.Bit32Lib;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.lib.CoreLibraries;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.*;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_languages.lua.callback_types.LuaCallback;
import org.figuramc.figura_core.script_languages.lua.callback_types.convert.CallbackItemToLua;
import org.figuramc.figura_core.script_languages.lua.callback_types.convert.LuaToCallbackItem;
import org.figuramc.figura_core.script_languages.lua.errors.LuaAvatarError;
import org.figuramc.figura_core.script_languages.lua.errors.LuaEscaper;
import org.figuramc.figura_core.script_languages.lua.other_apis.*;
import org.figuramc.figura_core.script_languages.lua.type_apis.model_parts.FiguraPartAPI;
import org.figuramc.figura_core.script_languages.lua.type_apis.rendering.TextureAPI;
import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_core.util.functional.BiThrowingBiFunction;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.DelegateAllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class LuaRuntime extends LuaState implements ScriptRuntimeComponent<LuaRuntime> {

    public static final Type<LuaRuntime> TYPE = new Type<>("LUA_RUNTIME", LuaRuntime::create, AvatarEvents.TYPE, Textures.TYPE, Molang.TYPE, VanillaRendering.TYPE, EntityRoot.TYPE, HudRoot.TYPE, ManagerAccess.TYPE);

    // Map each module index to its environment, so we can initialize them
    private final Map<Integer, LuaTable> moduleEnvironments = new IdentityHashMap<>();
    // Metatables for Figura types
    public final FiguraMetatables figuraMetatables;
    // Converters for callback types
    public final LuaToCallbackItem luaToCallbackItem;
    public final CallbackItemToLua callbackItemToLua;
    // The avatar this runtime is attached to. If an error occurs, this is who to blame.
    public final Avatar<?> avatar; // Can also fetch its allocation tracker if needed!

    // Use this for the factory since it throws AvatarError
    public static LuaRuntime create(Avatar<?> avatar, AvatarModules modules) throws AvatarInitError, AvatarOutOfMemoryError {
        try {
            return new LuaRuntime(avatar, modules);
        } catch (LuaOOM oom) {
            throw new AvatarOutOfMemoryError();
        } catch (LuaError | LuaUncatchableError unexpected) {
            throw new AvatarInitError(FiguraException.INTERNAL_ERROR, new TranslatableItems.Items1<>("Failed to initialize Lua runtime"), unexpected);
        }
    }

    // Implementation of interruption handler that simply errors out when the avatar errors for another reason.
    // For example, if we overran a time limit.
    public static class LuaRuntimeInterruptHandler implements InterruptHandler {
        // Keep a reference to the state to prevent any accidental funny business
        private LuaState state;

        @Override
        public InterruptAction interrupted() throws LuaUncatchableError {
            // This is only called at all when the state has been interrupted by an external thread.
            // Unlike CC:Tweaked, we're not interested in suspending this state to continue later.
            // We'll simply hard-abort the avatar at this point.
            state.interrupt(); // Re-interrupt so we can't somehow get back in here again
            throw LuaEscaper.INSTANCE;
        }
    }

    /**
     * Create a Lua runtime, but do not run any user code yet!
     */
    private LuaRuntime(Avatar<?> avatar, AvatarModules modules) throws LuaError, LuaOOM, LuaUncatchableError, AvatarInitError {
        // Initialize as LuaState
        super(LuaState.builder()
                .interruptHandler(new LuaRuntimeInterruptHandler())
                .allocationTracker(avatar.allocationTracker == null ? null : new DelegateAllocationTracker<>(avatar.allocationTracker, AvatarOutOfMemoryError.class, LuaOOM::new))
        );
        // We know which avatar is associated with this runtime
        this.avatar = avatar;
        ((LuaRuntimeInterruptHandler) this.interruptHandler).state = this;

        // Fetch other components we depend on
        @Nullable Textures texturesComponent = avatar.getComponent(Textures.TYPE);
        @Nullable Molang molang = avatar.getComponent(Molang.TYPE);
        @Nullable VanillaRendering vanillaRendering = avatar.getComponent(VanillaRendering.TYPE);
        @Nullable EntityRoot entityRoot = avatar.getComponent(EntityRoot.TYPE);
        @Nullable HudRoot hudRoot = avatar.getComponent(HudRoot.TYPE);
        @Nullable ManagerAccess managerAccess = avatar.getComponent(ManagerAccess.TYPE);
        @Nullable AvatarEvents events = avatar.getComponent(AvatarEvents.TYPE);

        // Add type metatables. They're shared across modules, in the true globals
        figuraMetatables = new FiguraMetatables(this);
        figuraMetatables.addTypesTo(globals());

        luaToCallbackItem = new LuaToCallbackItem(this);
        callbackItemToLua = new CallbackItemToLua(this);

        // Add global APIs
        CoreLibraries.standardGlobals(this);
        Bit32Lib.add(this);
        FiguraMath.init(this);
        FiguraPrint.init(this);

        // Always present tables
        globals().rawset("client", ClientTable.create(this));
        globals().rawset("text", TextTable.create(this, molang));

        // Add tables for other components we have
        if (vanillaRendering != null) globals().rawset("vanilla", VanillaTable.create(this, vanillaRendering));
        if (managerAccess != null) globals().rawset("manager", ManagerTable.create(this, managerAccess));
        if (events != null) globals().rawset("events", EventsTable.create(this, events.eventListeners));

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

            // textures:
            if (texturesComponent != null) {
                LuaTable textures = ValueFactory.tableOf(this.allocationTracker);
                _ENV.rawset("textures", textures);
                List<AvatarTexture> moduleTextures = texturesComponent.getTextures(module.index);
                for (int i = 0; i < module.materials.textures().size(); i++) {
                    String texName = module.materials.textures().get(i).name();
                    if (texName != null) textures.rawset(texName, TextureAPI.wrap(moduleTextures.get(i), this));
                }
            }

            // models:
            LuaTable models = ValueFactory.tableOf(this.allocationTracker);
            _ENV.rawset("models", models);
            if (entityRoot != null) {
                models.rawset("ENTITY_ROOT", FiguraPartAPI.wrap(entityRoot.root, this));
                if (entityRoot.rootByModule[module.index] != null)
                    models.rawset("entity", FiguraPartAPI.wrap(entityRoot.rootByModule[module.index], this));
            }
            if (hudRoot != null) {
                models.rawset("HUD_ROOT", FiguraPartAPI.wrap(hudRoot.root, this));
                if (hudRoot.rootByModule[module.index] != null)
                    models.rawset("hud", FiguraPartAPI.wrap(hudRoot.rootByModule[module.index], this));
            }

            // Create require() for this module
            _ENV.rawset("require", FiguraRequire.createRequire(this, _ENV, module));
        }
    }

    // When the avatar errors, even when called from another thread,
    // we interrupt the script so it will throw and escape Lua
    @Override
    public void onError() {
        this.interrupt();
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
        } catch (LuaOOM oom) {
            throw new UnsupportedOperationException("TODO OOM Errors");
        } catch (LuaAvatarError wrapper) {
            throw wrapper.getCause();
        } catch (LuaEscaper escaper) {
            throw AvatarError.ESCAPER;
        } catch (LuaUncatchableError unknown) {
            throw new AvatarError(FiguraException.INTERNAL_ERROR, new TranslatableItems.Items1<>("Unexpected LuaUncatchableError"), unknown);
        }
    }

    // Helper methods to run a file defined in org/figuramc/figura_core/scripts/lua/
    public static Varargs runAssetFile(LuaState state, String name) throws LuaError, LuaOOM, LuaUncatchableError {
        return runAssetFile(state, name, Constants.NONE);
    }
    public static Varargs runAssetFile(LuaState state, String name, Varargs args) throws LuaError, LuaOOM, LuaUncatchableError {
        try(InputStream input = LuaRuntime.class.getClassLoader().getResourceAsStream("org/figuramc/figura_core/scripts/lua/" + name + ".lua")) {
            // Compile the file
            if (input == null) throw new LuaError("Figura is missing the builtin file \"" + name + ".lua\"", state.allocationTracker);
            LuaClosure c = LoadState.load(state, input, "=" + name.toUpperCase(), state.globals());
            // Execute the file
            return state.runNoYield(c, args);
        } catch (IOException e) {
            throw new LuaError("Figura is missing the builtin file \"" + name + ".lua\"", state.allocationTracker);
        } catch (CompileException e) {
            throw new LuaError("Figura failed to compile builtin file \"" + name + ".lua\": " + e.getMessage(), state.allocationTracker);
        }
    }

    // Helper to turn a java List into a LuaTable
    public <T, E1 extends Throwable, E2 extends Throwable> LuaTable listToTable(List<T> items, BiThrowingBiFunction<LuaRuntime, T, LuaValue, E1, E2> function) throws LuaOOM, E1, E2 {
        LuaTable tab = new LuaTable(items.size(), 0, allocationTracker);
        int i = 1;
        for (T item : items) tab.rawset(i++, function.apply(this, item));
        return tab;
    }

    public LuaTable stringListToTable(List<String> items) throws LuaOOM {
        LuaTable tab = new LuaTable(items.size(), 0, allocationTracker);
        int i = 1;
        for (String item : items) tab.rawset(i++, LuaString.valueOf(allocationTracker, item));
        return tab;
    }

    // Calls toString() on each item
    public LuaTable stringifyListToTable(List<?> items) throws LuaOOM {
        LuaTable tab = new LuaTable(items.size(), 0, allocationTracker);
        int i = 1;
        for (Object item : items) tab.rawset(i++, LuaString.valueOf(allocationTracker, item.toString()));
        return tab;
    }

    // Try to convert a String into an EnumLike, or error on failure
    public <E extends EnumLike> @NotNull E stringToEnum(Class<E> clazz, String string) throws LuaError, LuaOOM {
        @Nullable E res =  EnumLike.byName(clazz, string);
        if (res == null) throw new LuaError("String \"" + string + "\" is not a member of enum " + clazz.getSimpleName(), allocationTracker); // TODO translate
        return res;
    }


}
