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

    // () -> bool
    // Runs at the END of every client tick (20 times per second).
    public static final Event<CallbackItem.Unit> CLIENT_TICK = new Event<>("client_tick", CallbackType.Unit.INSTANCE);

    // f32 -> bool
    // Runs at the BEGINNING of every frame. The passed number is the tick progress, often called "tick delta", the progress 0 to 1 between the previous tick and the current tick.
    public static final Event<CallbackItem.F32> CLIENT_RENDER = new Event<>("client_render", CallbackType.F32.INSTANCE);

    // Entity -> bool
    public static final Event<EntityView<?>> ENTITY_TICK = new Event<>("entity_tick", CallbackType.Entity.INSTANCE);

    // (f32, Entity) -> bool
    // Runs during entity rendering, at a specific point after the entity has been posed, but before its vanilla model been drawn.
    public static final Event<CallbackItem.Tuple2<CallbackItem.F32, EntityView<?>>> ENTITY_RENDER = new Event<>("entity_render", new CallbackType.Tuple2<>(CallbackType.F32.INSTANCE, CallbackType.Entity.INSTANCE));


}
