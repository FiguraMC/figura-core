package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.model.texture.AvatarTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * A component which holds all the materials for an Avatar.
 * (Similar to Textures component)
 */
public class Materials implements AvatarComponent<Materials> {

    public static final Type<Materials> TYPE = new Type<>(Materials::new, Textures.TYPE);
    public Type<Materials> getType() { return TYPE; }

    private final List<List<FiguraRenderType>> materials; // materials[moduleIndex][materialIndex]

    public Materials(Avatar<?> avatar, AvatarModules modules) throws AvatarOutOfMemoryError {
        Textures texturesComponent = avatar.assertComponent(Textures.TYPE);

        materials = new ArrayList<>();
        for (var module : modules.loadTimeModules()) {
            // Fetch textures for this module
            int moduleIndex = materials.size();
            List<AvatarTexture> textures = texturesComponent.getTextures(moduleIndex);
            // Create materials
            ArrayList<FiguraRenderType> moduleMaterials = new ArrayList<>();
            for (ModuleMaterials.MaterialMaterials mats : module.materials.materials())
                moduleMaterials.add(FiguraRenderType.from(mats, textures, avatar.allocationTracker));
            materials.add(moduleMaterials);
        }
    }

    public List<FiguraRenderType> getMaterials(int moduleIndex) {
        return materials.get(moduleIndex);
    }

    public FiguraRenderType getMaterial(int moduleIndex, int materialIndex) {
        return getMaterials(moduleIndex).get(materialIndex);
    }

}
