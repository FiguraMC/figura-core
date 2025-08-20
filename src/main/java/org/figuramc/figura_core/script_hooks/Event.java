package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.util.enumlike.EnumLike;

import java.util.LinkedHashMap;

/**
 * Contains all events recognized by the Figura mod.
 * All Event constructors must be run before the call to EnumLike.freeze(Event.class)!
 */
public final class Event<Args extends CallbackItem> extends EnumLike {

    public final String name;
    public final CallbackType.Func<Args, CallbackItem.Bool> type; // The type of the event signature

    public static final LinkedHashMap<String, Event<?>> EVENTS_BY_NAME = new LinkedHashMap<>();

    public Event(String name, CallbackType<Args> paramType) {
        if (EVENTS_BY_NAME.put(name, this) != null)
            throw new IllegalArgumentException("Event named \"" + name + "\" already exists! Please use some kind of disambiguation!");
        this.name = name;
        this.type = new CallbackType.Func<>(paramType, CallbackType.Bool.INSTANCE); // All events return bool
    }

    // All event listeners return bool.
    // If it returns true, the listener is removed from the registered list.

    // ----- TICK EVENTS -----

    // () -> bool
    // Runs at the END of every client tick.
    // This is so when rendering, avatars have the most recent information to go off of (since values were updated during this tick)
    public static final Event<CallbackItem.Unit> CLIENT_TICK = new Event<>("client_tick", CallbackType.Unit.INSTANCE);
    // Entity -> bool
    // Runs AFTER the entity associated with the avatar is ticked.
    public static final Event<EntityView<?>> ENTITY_TICK = new Event<>("entity_tick", CallbackType.Entity.INSTANCE);

    // ----- RENDER EVENTS -----

    // (tickDelta: f32) -> bool
    // Earliest thing to run every frame. The passed number is the tick progress, often called "tick delta", the progress 0 to 1 between the previous tick and the current tick.
    // It runs near the beginning of the frame because we want an injection point early enough to modify the camera and other per-frame things without being a frame late.
    public static final Event<CallbackItem.F32> CLIENT_RENDER = new Event<>("client_render", CallbackType.F32.INSTANCE);
    // (tickDelta: f32) -> bool
    // Runs just before the world is rendered, but after things like the camera are set up by client_render.
    // This is useful if you want to READ per-frame values like the targeted entity after they've been set up.
    public static final Event<CallbackItem.F32> WORLD_RENDER = new Event<>("world_render", CallbackType.F32.INSTANCE);


    // (tickDelta: f32, Entity) -> bool
    // Runs during entity rendering, at a point after the entity's vanilla model has been posed, but before its vanilla model been drawn.
    // This is to give the avatar a chance to both read the vanilla model's pose and write to it before it's drawn this frame.
    public static final Event<CallbackItem.Tuple2<CallbackItem.F32, EntityView<?>>> ENTITY_RENDER = new Event<>("entity_render", new CallbackType.Tuple2<>(CallbackType.F32.INSTANCE, CallbackType.Entity.INSTANCE));

}
