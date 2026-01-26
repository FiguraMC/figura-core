package org.figuramc.figura_core.script_languages.lua;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.cc.tweaked.cobalt.internal.unwind.SuspendedAction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.Dispatch;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction;
import org.figuramc.figura_core.script_languages.lua.generated.*;
import org.figuramc.figura_core.util.functional.BiThrowingBiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants.INDEX;

/**
 * Class where metatables for wrapped java types are stored
 */
public class FiguraMetatables {

    // Fields containing metatables, to access quickly java-side

    // General
    public final LuaTable eventListener; // EventListener
    public final LuaTable eventListenerCallbackHandle; // EventListener.CallbackHandle

    // Callbacks and callback items
    public final LuaTable callbackType; // CallbackType
    public final LuaTable stringView; // StringView
    public final LuaTable listView; // ListView
    public final LuaTable mapView; // MapView
    public final LuaTable callbackView; // ScriptCallback

    // Minecraft callback items
    public final LuaTable entityView;
    public final LuaTable blockStateView;
    public final LuaTable worldView;
    public final LuaTable itemStackView;

    // Math objects
    public final LuaTable vec2;
    public final LuaTable vec3;
    public final LuaTable vec4;

    // Model parts
    public final LuaTable transformable;
    public final LuaTable figuraPart;
    public final LuaTable figmodelModelPart;

    // Textures
    public final LuaTable texture;
    public final LuaTable material;

    // Vanilla rendering
    public final LuaTable vanillaPart;
//    public final LuaTable vanillaRenderLayer;

    // Animation stuff
    public final LuaTable animationInstance;

    // Manager special
    public final LuaTable avatarListEntry;


    public FiguraMetatables(LuaRuntime state) throws LuaError, LuaOOM {
        // General
        eventListener = API__EventListener.createMetatable(state);
        eventListenerCallbackHandle = API__EventListenerCallbackHandle.createMetatable(state);

        // Callbacks
        callbackType = API__CallbackType.createMetatable(state);
        stringView = API__StringView.createMetatable(state);
        listView = API__ListView.createMetatable(state);
        mapView = API__MapView.createMetatable(state);
        callbackView = API__CallbackView.createMetatable(state);

        // Minecraft items
        entityView = API__Entity.createMetatable(state);
        blockStateView = API__BlockState.createMetatable(state);
        worldView = API__World.createMetatable(state);
        itemStackView = API__ItemStack.createMetatable(state);

        // Math objects
        vec2 = API__Vec2.createMetatable(state);
        vec3 = API__Vec3.createMetatable(state);
        vec4 = API__Vec4.createMetatable(state);

        // Model part
        transformable = API__Transformable.createMetatable(state);
        figuraPart = API__FiguraPart.createMetatable(state, transformable);
        figmodelModelPart = API__Figmodel.createMetatable(state, figuraPart);

        // Texture
        texture = API__Texture.createMetatable(state);
        material = API__Material.createMetatable(state);

        // Vanilla rendering
        vanillaPart = API__VanillaPart.createMetatable(state, transformable);

        // Animations
        animationInstance = API__AnimationInstance.createMetatable(state);

        // Manager special (TODO consider not even putting this in for Avatars without manager access?)
        avatarListEntry = API__AvatarListEntry.createMetatable(state);
    }

    // Add all the type metatables, with PascalCase keys, to the given table
    public void addTypesTo(LuaTable table) throws LuaOOM {
        table.rawset("EventListener", eventListener);

        table.rawset("Callback", callbackView);
        table.rawset("CallbackType", callbackType);
        table.rawset("StringView", stringView);
        table.rawset("ListView", listView);

        table.rawset("Entity", entityView);

        table.rawset("Vec2", vec2);
        table.rawset("Vec3", vec3);
        table.rawset("Vec4", vec4);

        table.rawset("Transformable", transformable);
        table.rawset("FiguraPart", figuraPart);
        table.rawset("Figmodel", figmodelModelPart);

        table.rawset("Texture", texture);
        table.rawset("Material", material);

        table.rawset("VanillaPart", vanillaPart);

        table.rawset("AnimationInstance", animationInstance);
    }

    // Helpers to ensure safe use
    public static void setupIndexing(LuaState state, LuaTable metatable) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, null, null, null);
    }

    public static void setupIndexingWithSuperclass(LuaState state, LuaTable metatable, @NotNull LuaTable superclassMetatable) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, Objects.requireNonNull(superclassMetatable), null, null);
    }

    public static void setupIndexingWithCustomIndexer(LuaState state, LuaTable metatable, @NotNull LuaFunction customIndexer) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, null, Objects.requireNonNull(customIndexer), null);
    }

    public static void setupIndexingWithPreIndexer(LuaState state, LuaTable metatable, @NotNull LuaTable superclassMetatable, @NotNull LuaFunction customPreIndexer) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, Objects.requireNonNull(superclassMetatable), null, Objects.requireNonNull(customPreIndexer));
    }

    public static void setupIndexingWithSuperclassAndCustomIndexer(LuaState state, LuaTable metatable, @NotNull LuaTable superclassMetatable, @NotNull LuaFunction customIndexer) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, Objects.requireNonNull(superclassMetatable), Objects.requireNonNull(customIndexer), null);
    }

    public static void setupIndexingWithSuperclassAndCustomIndexerAndPreIndexer(LuaState state, LuaTable metatable, @NotNull LuaTable superclassMetatable, @NotNull LuaFunction customIndexer, @NotNull LuaFunction customPreIndexer) throws LuaError, LuaOOM {
        setupIndexingImpl(state, metatable, Objects.requireNonNull(superclassMetatable), Objects.requireNonNull(customIndexer), Objects.requireNonNull(customPreIndexer));
    }

    // Helper method to set up inheritance relationship between subclass and superclass metatables,
    // and also deals with any custom __index implementations.
    // Make sure to call this on EVERY created metatable, even ones without superclasses, so that indexing works as it should.
    // This should also be the last thing called in the class, I think. (Maybe it doesn't matter...?)
    private static void setupIndexingImpl(LuaState state, LuaTable thisMetatable, @Nullable LuaTable superclassMetatable, @Nullable LuaFunction customIndexer, @Nullable LuaFunction customPreIndexer) throws LuaError, LuaOOM {
        // If there's no superclass metatable, and no custom indexer, just make it simple.
        if (superclassMetatable == null && customIndexer == null && customPreIndexer == null) {
            thisMetatable.rawset(INDEX, thisMetatable); // Set __index to itself, and we're done
            return;
        }
        // If there's a superclass involved:
        if (superclassMetatable != null) {
            // If there's a superclass involved, make sure to copy all metamethods from the superclass
            // (lua doesn't provide a way to do this any better, sadly :/)
            forEach(superclassMetatable, (k, v) -> {
                if (!k.isString()) return;
                if (!thisMetatable.rawget(k).isNil()) return; // If subclass overrides this metamethod, ignore
                String method = k.checkString(state);
                if (method.equals("__index")) return; // __index handled separately
                if (method.equals("__name")) return; // __name shouldn't be inherited
                if (method.startsWith("__")) thisMetatable.rawset(k, v); // Copy other metamethods into subclass
            });
            // Deal with the __index function separately
            LuaValue superclassIndex = superclassMetatable.rawget(INDEX);
            // If the superclass has __index as a function:
            if (superclassIndex instanceof LuaFunction superIndexFunc) {
                // If we also have a custom __index, we need to weave them together:
                if (customIndexer != null || customPreIndexer != null) {
                    thisMetatable.rawset(INDEX, LibFunction.createS((s, di, args) -> {
                        // First, if we have a pre-indexer, try to run it, and return if we found one.
                        if (customPreIndexer != null) {
                            LuaValue preIndexerResult = SuspendedAction.run(di, () -> Dispatch.invoke(s, customPreIndexer, args)).first();
                            if (!preIndexerResult.isNil()) return preIndexerResult;
                        }
                        // Second, try to fetch a method:
                        LuaValue k = args.arg(2);
                        LuaValue method = SuspendedAction.run(di, () -> OperationHelper.getTable(s, thisMetatable, k)).first();
                        if (!method.isNil()) return method;
                        // Defer to superclass indexer:
                        LuaValue superclassResult = SuspendedAction.run(di, () -> Dispatch.invoke(s, superIndexFunc, args)).first();
                        if (!superclassResult.isNil()) return superclassResult;
                        // If superclass indexer found nothing, try our custom indexer (if we have one):
                        if (customIndexer != null) {
                            LuaValue subclassIndexerResult = SuspendedAction.run(di, () -> Dispatch.invoke(s, customIndexer, args)).first();
                            if (!subclassIndexerResult.isNil()) return subclassIndexerResult;
                        }
                        // Default to nil.
                        return Constants.NIL;
                    }));
                } else {
                    // We don't have a custom indexer or preindexer, so just defer to the superclass's.
                    thisMetatable.rawset(INDEX, LibFunction.createS((s, di, args) -> {
                        // First, try to fetch a method:
                        LuaValue k = args.arg(2);
                        LuaValue method = SuspendedAction.run(di, () -> OperationHelper.getTable(s, thisMetatable, k)).first();
                        if (!method.isNil()) return method;
                        // If no method was found, fall back to the superclass's indexer.
                        return SuspendedAction.run(di, () -> Dispatch.invoke(s, superIndexFunc, args));
                    }));
                }
                return;
            } else {
                // The superclass does not have a custom __index function, so we can use table-based inheritance
                LuaTable subclassMetatableMetatable = ValueFactory.tableOf(state.allocationTracker);
                subclassMetatableMetatable.rawset(INDEX, superclassMetatable);
                thisMetatable.setMetatable(state, subclassMetatableMetatable);
            }
        }
        // If we have a custom index function, wire it up:
        if (customIndexer != null || customPreIndexer != null) {
            thisMetatable.rawset(INDEX, LibFunction.createS((s, di, args) -> {
                // First, if we have a pre-indexer, try to run it, and return if we found one.
                if (customPreIndexer != null) {
                    LuaValue preIndexerResult = SuspendedAction.run(di, () -> Dispatch.invoke(s, customPreIndexer, args)).first();
                    if (!preIndexerResult.isNil()) return preIndexerResult;
                }
                // Second, try to fetch a method:
                LuaValue k = args.arg(2);
                LuaValue method = SuspendedAction.run(di, () -> OperationHelper.getTable(s, thisMetatable, k)).first();
                if (!method.isNil()) return method;
                // If no method was found, try our indexer (if we have one):
                if (customIndexer != null) {
                    LuaValue subclassIndexerResult = SuspendedAction.run(di, () -> Dispatch.invoke(s, customIndexer, args)).first();
                    if (!subclassIndexerResult.isNil()) return subclassIndexerResult;
                }
                // Return nil by default.
                return Constants.NIL;
            }));
        } else {
            thisMetatable.rawset(INDEX, thisMetatable);
        }
    }

    private static void forEach(LuaTable table, BiThrowingBiConsumer<LuaValue, LuaValue, LuaError, LuaOOM> consumer) throws LuaError, LuaOOM {
        LuaValue k = Constants.NIL;
        while ( true ) {
            Varargs n = table.next(k);
            if ( (k = n.first()).isNil() )
                break;
            LuaValue v = n.arg(2);
            consumer.accept(k, v);
        }
    }


}
