package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.enumlike.IdMap;

/**
 * Holds the built-in EventListener instances
 */
public class AvatarEvents implements AvatarComponent<AvatarEvents> {

    public static final Type<AvatarEvents> TYPE = new Type<>("AVATAR_EVENTS", AvatarEvents::new);

    // Listeners for built-in events, indexed using the event's ID.
    // When getting items, use the type-safe getEventListener() function instead.
    public final IdMap<Event<?, ?>, EventListener<?, ?>> eventListeners;

    public AvatarEvents(Avatar<?> avatar, AvatarModules modules) {
        eventListeners = new IdMap<>(Event.class, e -> new EventListener<>(avatar, e.type));
    }

    // Type-safe EventListener<T> getter from Event<T>
    @SuppressWarnings("unchecked")
    public <T extends CallbackItem, R extends CallbackItem> EventListener<T, R> getEventListener(Event<T, R> event) {
        return (EventListener<T, R>) eventListeners.get(event);
    }

}
