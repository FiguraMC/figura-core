package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.model.renderers.Renderable;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

public class EntityRoot implements AvatarComponent<EntityRoot> {

    public static final Type<EntityRoot> TYPE = new Type<>(EntityRoot::new, Textures.TYPE, Molang.TYPE, VanillaRendering.TYPE);
    public Type<EntityRoot> getType() { return TYPE; }

    public final Renderable<FiguraModelPart> root;
    public final @Nullable FiguraModelPart[] rootByModule;

    // Vanilla rendering parameter if possible, this will allow mimics to work
    public EntityRoot(Avatar<?> avatar, AvatarModules modules) throws AvatarError {
        @Nullable AllocationTracker<AvatarError> allocationTracker = avatar.allocationTracker;
        Textures texturesComponent = avatar.assertComponent(Textures.TYPE);
        Molang molang = avatar.assertComponent(Molang.TYPE);
        @Nullable VanillaRendering vanillaRendering = avatar.getComponent(VanillaRendering.TYPE);

        // Wrap the entity roots of each module into a new wrapper part
        // Also store the roots by module for other accessors (like scripts)
        LinkedHashMap<String, FiguraModelPart> roots = new LinkedHashMap<>();
        rootByModule = new FiguraModelPart[modules.loadTimeModules().size()];
        for (AvatarModules.LoadTimeModule mod : modules.loadTimeModules()) {
            if (mod.materials.entityRoot() == null) continue;
            FiguraModelPart part = new FiguraModelPart(mod, mod.materials.entityRoot(), allocationTracker, texturesComponent, molang, vanillaRendering);
            // Also store the parts in the module objects to be later accessed
            rootByModule[mod.index] = part;
            roots.put(Integer.toString(mod.index), part);
        }

        // Return a wrapper around each of them
        this.root = new Renderable<>(new FiguraModelPart(roots, allocationTracker));
    }

    @Override
    public void destroy() {
        root.destroy();
    }
}
