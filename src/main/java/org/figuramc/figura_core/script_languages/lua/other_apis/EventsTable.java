package org.figuramc.figura_core.script_languages.lua.other_apis;

import org.figuramc.figura_cobalt.LuaOOM;
import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.*;
import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.EventListener;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.script_languages.lua.type_apis.EventListenerAPI;

import java.util.Map;

/**
 * The "events" table contains the built-in EventListener instances.
 * It also has special helper syntax for registration via "function events.event_name() ...code... end".
 */
public class EventsTable {

    public static LuaTable create(LuaRuntime state, Map<Event<?, ?>, EventListener<?, ?>> eventListeners) throws LuaError, LuaOOM {

        // Create tables and set up meta-stuff
        LuaTable events = new LuaTable(state.allocationTracker); // Dummy table, which has metatable __index and __newindex
        LuaTable byName = new LuaTable(state.allocationTracker); // Backing table used as __index and for __newindex registration
        LuaTable metatable = new LuaTable(state.allocationTracker); // Metatable
        events.setMetatable(state, metatable);
        metatable.rawset(Constants.INDEX, byName);
        metatable.rawset(Constants.NEWINDEX, LibFunction.create((s, eventsTab, name, func) -> {
            LuaValue event = byName.rawget(name);
            if (event.isNil()) { throw new LuaError("Event named \"" + name + "\" does not exist", s.allocationTracker); }
            EventListener<?, ?> eventListener = event.checkUserdata(s, EventListener.class);
            EventListenerAPI.registerImpl((LuaRuntime) s, eventListener, func);
            return Constants.NIL;
        }));

        // Fill in the backing table with the built-in listeners provided
        for (var entry : eventListeners.entrySet()) {
            byName.rawset(entry.getKey().name.toLowerCase(), EventListenerAPI.wrap(entry.getValue(), state));
        }
        return events;
    }

}
