package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HudRoot implements AvatarComponent<HudRoot> {

    public static final Type<HudRoot> TYPE = new Type<>("HUD_ROOT", HudRoot::new, RenderDataHolder.TYPE, Textures.TYPE, Materials.TYPE, Molang.TYPE, VanillaRendering.TYPE);
    public Type<HudRoot> getType() { return TYPE; }

    public final FiguraModelPart root;
    public final @Nullable FiguraModelPart[] rootByModule;

    // Vanilla rendering parameter if possible, this will allow mimics to work
    public HudRoot(Avatar<?> avatar, AvatarModules modules) throws AvatarInitError, AvatarOutOfMemoryError {

        // Wrap the hud roots of each module into a new wrapper part
        // Also store the roots by module for other accessors (like scripts)
        List<FiguraModelPart> roots = new ArrayList<>();
        rootByModule = new FiguraModelPart[modules.loadTimeModules().size()];
        for (AvatarModules.LoadTimeModule mod : modules.loadTimeModules()) {
            if (mod.materials.hudRoot() == null) continue;
            FiguraModelPart part = new FiguraModelPart(avatar, "", mod, mod.materials.hudRoot());
            // Also store the parts in the module objects to be later accessed
            rootByModule[mod.index] = part;
            roots.add(part);
        }

        // Return a wrapper around all of them
        root = new FiguraModelPart(avatar, "", roots);
        // Make sure to build and save in the render data holder
        AvatarInitError.wrapAvatarError(root::buildRenderingData);
    }
}
