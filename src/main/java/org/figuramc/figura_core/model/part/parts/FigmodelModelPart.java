package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.animation.Animation;
import org.figuramc.figura_core.animation.AnimationInstance;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.data.ModuleMaterials;
import org.figuramc.figura_core.model.texture.AvatarTexture;
import org.figuramc.figura_core.util.MapUtils;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A model part which corresponds to a ".figmodel" file in the hierarchy.
 * It acts as a regular model part, but also has additional information captured from the model file.
 */
public class FigmodelModelPart extends FiguraModelPart {

    // Animations are pre-bound, because they come bundled with the figmodel!
    private final Map<String, AnimationInstance> animations;
    // Map to textures; these may be the same object reference as other textures.
    private final Map<String, AvatarTexture> textures;

    public FigmodelModelPart(String name, AvatarModules.LoadTimeModule module, ModuleMaterials.FigmodelMaterials materials, @Nullable AllocationTracker<AvatarError> allocationTracker, Textures texturesComponent, Molang molangState, @Nullable VanillaRendering vanillaComponent) throws AvatarError {
        super(name, module, materials, allocationTracker, texturesComponent, molangState, vanillaComponent);
        animations = MapUtils.mapValues(materials.animations, (animName, animMats) -> new AnimationInstance(new Animation(name, animName, animMats, molangState, allocationTracker), this, allocationTracker));
        textures = MapUtils.mapValues(materials.textures, texIndex -> texturesComponent.getTexture(module.index, texIndex));

        if (allocationTracker != null) {
            for (var key : animations.keySet())
                allocationTracker.track(key);
            for (var key : textures.keySet())
                allocationTracker.track(key);
            allocationTracker.track(this, AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE * 2);
        }
    }

    public @Nullable AnimationInstance animation(String name) { return animations.get(name); }
    public @Nullable AvatarTexture texture(String name) { return textures.get(name); }

}
