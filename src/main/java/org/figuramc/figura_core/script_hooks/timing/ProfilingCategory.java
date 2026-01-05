package org.figuramc.figura_core.script_hooks.timing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProfilingCategory {

    // Roots
    public static final List<ProfilingCategory> ROOTS = new ArrayList<>();

    // Each element in the hierarchy has a name
    public final String name;
    public final String fullName;
    public final @Nullable ProfilingCategory parent;
    public final List<ProfilingCategory> children = new ArrayList<>();

    // Constants

    // Init
    public static final ProfilingCategory INITIALIZATION = new ProfilingCategory("initialization");

    // Ticking
    public static final ProfilingCategory TICKING = new ProfilingCategory("ticking");
        public static final ProfilingCategory TICKING_SCRIPTS = new ProfilingCategory("scripts", TICKING);
            // Tick events
            public static final ProfilingCategory CLIENT_TICK_EVENT = new ProfilingCategory("client_tick", TICKING_SCRIPTS);
            public static final ProfilingCategory WORLD_TICK_EVENT = new ProfilingCategory("world_tick", TICKING_SCRIPTS);
            public static final ProfilingCategory ENTITY_TICK_EVENT = new ProfilingCategory("entity_tick", TICKING_SCRIPTS);

    // Rendering
    public static final ProfilingCategory RENDERING = new ProfilingCategory("rendering");
        public static final ProfilingCategory DRAWING = new ProfilingCategory("drawing", RENDERING);
        public static final ProfilingCategory RENDERING_SCRIPTS = new ProfilingCategory("scripts", RENDERING);
            // Render events
            public static final ProfilingCategory CLIENT_RENDER_EVENT = new ProfilingCategory("client_render", RENDERING_SCRIPTS);
            public static final ProfilingCategory WORLD_RENDER_EVENT = new ProfilingCategory("world_render", RENDERING_SCRIPTS);
            public static final ProfilingCategory ENTITY_RENDER_EVENT = new ProfilingCategory("entity_render", RENDERING_SCRIPTS);

    public ProfilingCategory(String name) {
        this.name = this.fullName = name;
        this.parent = null;
        ROOTS.add(this);
    }

    public ProfilingCategory(String name, @NotNull ProfilingCategory parent) {
        this.name = name;
        this.fullName = parent.fullName + "/" + name;
        this.parent = parent;
        this.parent.children.add(this);
    }

}
