package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntityRoot implements AvatarComponent<EntityRoot> {

    public static final Type<EntityRoot> TYPE = new Type<>(EntityRoot::new, Textures.TYPE, Materials.TYPE, Molang.TYPE, VanillaRendering.TYPE);
    public Type<EntityRoot> getType() { return TYPE; }

    public final RenderingRoot<FiguraModelPart> root;
    public final @Nullable FiguraModelPart[] rootByModule;

    // Vanilla rendering parameter if possible, this will allow mimics to work
    public EntityRoot(Avatar<?> avatar, AvatarModules modules) throws AvatarError {
        @Nullable AllocationTracker<AvatarError> allocationTracker = avatar.allocationTracker;
        Textures texturesComponent = avatar.assertComponent(Textures.TYPE);
        Materials materialsComponent = avatar.assertComponent(Materials.TYPE);
        Molang molang = avatar.assertComponent(Molang.TYPE);
        @Nullable VanillaRendering vanillaRendering = avatar.getComponent(VanillaRendering.TYPE);

        // Wrap the entity roots of each module into a new wrapper part
        // Also store the roots by module for other accessors (like scripts)
        List<FiguraModelPart> roots = new ArrayList<>();
        rootByModule = new FiguraModelPart[modules.loadTimeModules().size()];
        for (AvatarModules.LoadTimeModule mod : modules.loadTimeModules()) {
            if (mod.materials.entityRoot() == null) continue;
            FiguraModelPart part = new FiguraModelPart("", mod, mod.materials.entityRoot(), allocationTracker, texturesComponent, materialsComponent, molang, vanillaRendering);
            // Also store the parts in the module objects to be later accessed
            rootByModule[mod.index] = part;
            roots.add(part);
        }

        // Return a wrapper around all of them
        FiguraModelPart realRoot = new FiguraModelPart("", roots, allocationTracker);
        realRoot.getTransform().setScale(1f / 16); // Scale by 1/16 for entity!
        this.root = new RenderingRoot<>(realRoot, allocationTracker);
    }

    @Override
    public void destroy() {
        root.destroy();
    }
}
