package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.*;
import org.figuramc.figura_core.util.enumlike.EnumLike;

import java.util.LinkedHashMap;

/**
 * Contains all events recognized by the Figura mod.
 * All Event constructors must be run before the call to EnumLike.freeze(Event.class)!
 */
public final class Event<Args extends CallbackItem, ReturnType extends CallbackItem> extends EnumLike {

    public final String name;
    public final CallbackType.Func<Args, ReturnType> type; // The type of the event signature

    public static final LinkedHashMap<String, Event<?, ?>> EVENTS_BY_NAME = new LinkedHashMap<>();

    public Event(String name, CallbackType<Args> paramType, CallbackType<ReturnType> returnType) {
        if (EVENTS_BY_NAME.put(name, this) != null)
            throw new IllegalArgumentException("Event named \"" + name + "\" already exists! Please use some kind of disambiguation!");
        this.name = name;
        this.type = new CallbackType.Func<>(paramType, returnType);
    }

    // ----- TICK EVENTS -----

    // () -> ()
    // Runs at the END of every client tick.
    // This is so when rendering, avatars have the most recent information to go off of (since values were updated during this tick)
    public static final Event<CallbackItem.Unit, CallbackItem.Unit> CLIENT_TICK = new Event<>("client_tick", CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE);
    // Entity -> ()
    // Runs AFTER the entity associated with the avatar is ticked.
    public static final Event<CallbackItem.Tuple2<EntityView<?>, WorldView<?>>, CallbackItem.Unit> ENTITY_TICK = new Event<>("entity_tick", new CallbackType.Tuple2<>(CallbackType.Entity.INSTANCE, CallbackType.World.INSTANCE), CallbackType.Unit.INSTANCE);

    // ----- RENDER EVENTS -----

    // Render events run on the main thread during entity extraction, and return callbacks
    // to run at an unspecified time on the render thread.
    private static final CallbackType.Tuple2<CallbackItem.Optional<FuncView<CallbackItem, CallbackItem.Unit>>, CallbackItem> RENDER_EVENT_RETURN_TYPE
            = new CallbackType.Tuple2<>(new CallbackType.Optional<>(new CallbackType.Func<>(CallbackType.Any.INSTANCE, CallbackType.Unit.INSTANCE)), CallbackType.Any.INSTANCE);

    // (tickDelta: f32) -> (Option<Any -> ()>, Any)
    // Earliest thing to run every frame. The passed number is the tick progress, often called "tick delta", the progress 0 to 1 between the previous tick and the current tick.
    // It runs near the beginning of the frame because we want an injection point early enough to modify the camera and other per-frame things without being a frame late.
    public static final Event<
            CallbackItem.F32,
            CallbackItem.Tuple2<CallbackItem.Optional<FuncView<CallbackItem, CallbackItem.Unit>>, CallbackItem>
    > CLIENT_RENDER = new Event<>("client_render", CallbackType.F32.INSTANCE, RENDER_EVENT_RETURN_TYPE);
    // (tickDelta: f32) -> (Option<Any -> ()>, Any)
    // Runs just before the world is rendered, but after things like the camera are set up by client_render.
    // This is useful if you want to READ per-frame values like the targeted entity after they've been set up.
    public static final Event<
            CallbackItem.F32,
            CallbackItem.Tuple2<CallbackItem.Optional<FuncView<CallbackItem, CallbackItem.Unit>>, CallbackItem>
    > WORLD_RENDER = new Event<>("world_render", CallbackType.F32.INSTANCE, RENDER_EVENT_RETURN_TYPE);
    // (tickDelta: f32, Entity) -> (Option<Any -> ()>, Any)
    // Runs during entity rendering, at a point after the entity's vanilla model has been posed, but before its vanilla model been drawn.
    // This is to give the avatar a chance to both read the vanilla model's pose and write to it before it's drawn this frame.
    public static final Event<
            CallbackItem.Tuple2<CallbackItem.F32, EntityView<?>>,
            CallbackItem.Tuple2<CallbackItem.Optional<FuncView<CallbackItem, CallbackItem.Unit>>, CallbackItem>
    > ENTITY_RENDER = new Event<>("entity_render", new CallbackType.Tuple2<>(CallbackType.F32.INSTANCE, CallbackType.Entity.INSTANCE), RENDER_EVENT_RETURN_TYPE);

    // ----- Input Events -----

    // (button: i32, action: i32, modifiers: i32, cancel: () -> ()) -> ()
    // Runs on host/gui avatars when the mouse is pressed down or released.
    // If you call the 'cancel' callback argument, the normal operation will be canceled.
    public static final Event<CallbackItem.Tuple4<
            CallbackItem.I32, CallbackItem.I32, CallbackItem.I32,
            FuncView<CallbackItem.Unit, CallbackItem.Unit>
    >, CallbackItem.Unit> MOUSE_PRESS = new Event<>("mouse_press", new CallbackType.Tuple4<>(
            CallbackType.I32.INSTANCE, CallbackType.I32.INSTANCE, CallbackType.I32.INSTANCE,
            new CallbackType.Func<>(CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE)
    ), CallbackType.Unit.INSTANCE);
    // (x: f64, y: f64, cancel: () -> ()) -> ()
    // Runs on host/gui avatars when the mouse is moved.
    // If you call the 'cancel' callback argument, the normal operation will be canceled.
    public static final Event<CallbackItem.Tuple3<
            CallbackItem.F64, CallbackItem.F64,
            FuncView<CallbackItem.Unit, CallbackItem.Unit>
    >, CallbackItem.Unit> MOUSE_MOVE = new Event<>("mouse_move", new CallbackType.Tuple3<>(
            CallbackType.F64.INSTANCE, CallbackType.F64.INSTANCE,
            new CallbackType.Func<>(CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE)
    ), CallbackType.Unit.INSTANCE);
    // (scroll_down: bool, cancel: () -> ()) -> ()
    // Runs on host/gui avatars when the mouse wheel is scrolled. If scrolled down, passes true, otherwise passes false.
    // If you call the 'cancel' callback argument, the normal operation will be canceled.
    public static final Event<CallbackItem.Tuple2<
            CallbackItem.Bool,
            FuncView<CallbackItem.Unit, CallbackItem.Unit>
    >, CallbackItem.Unit> MOUSE_SCROLL = new Event<>("mouse_scroll", new CallbackType.Tuple2<>(
            CallbackType.Bool.INSTANCE,
            new CallbackType.Func<>(CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE)
    ), CallbackType.Unit.INSTANCE);
    // (button: i32, action: i32, modifiers: i32, cancel: () -> ()) -> ()
    // Runs on host/gui avatars when a key is pressed, held, or released.
    // If you call the 'cancel' callback argument, the normal operation will be canceled.
    public static final Event<CallbackItem.Tuple4<
            CallbackItem.I32, CallbackItem.I32, CallbackItem.I32,
            FuncView<CallbackItem.Unit, CallbackItem.Unit>
    >, CallbackItem.Unit> KEY_PRESS = new Event<>("key_press", new CallbackType.Tuple4<>(
            CallbackType.I32.INSTANCE, CallbackType.I32.INSTANCE, CallbackType.I32.INSTANCE,
            new CallbackType.Func<>(CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE)
    ), CallbackType.Unit.INSTANCE);
    // (codepoint: i32, cancel: () -> ()) -> ()
    // Runs on host/gui avatars when a character is typed. The character (may be multiple Java chars!) is passed as a String.
    // If you call the 'cancel' callback argument, the normal operation will be canceled.
    public static final Event<CallbackItem.Tuple2<
            StringView,
            FuncView<CallbackItem.Unit, CallbackItem.Unit>
    >, CallbackItem.Unit> CHAR_TYPED = new Event<>("char_typed", new CallbackType.Tuple2<>(
            CallbackType.Str.INSTANCE,
            new CallbackType.Func<>(CallbackType.Unit.INSTANCE, CallbackType.Unit.INSTANCE)
    ), CallbackType.Unit.INSTANCE);

}
