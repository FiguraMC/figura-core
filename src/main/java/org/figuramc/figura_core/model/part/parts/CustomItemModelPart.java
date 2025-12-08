package org.figuramc.figura_core.model.part.parts;

import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.components.Materials;
import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_core.avatars.components.Textures;
import org.figuramc.figura_core.avatars.components.VanillaRendering;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.ItemRenderContext;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * A root which works for rendering a custom item.
 * Contains information about how to transform the item in different contexts.
 */
public class CustomItemModelPart extends FigmodelModelPart {

    public final HashMap<ItemRenderContext, @Nullable Matrix4f> itemTransforms;

    public static final int SIZE_ESTIMATE =
            AllocationTracker.OBJECT_SIZE
            + AllocationTracker.REFERENCE_SIZE
            // Map
            + AllocationTracker.OBJECT_SIZE;

    public CustomItemModelPart(String name, AvatarModules.LoadTimeModule module, ModuleMaterials.FigmodelMaterials materials, LinkedHashMap<String, ModuleMaterials.ItemPartTransform> transforms, @Nullable AllocationTracker<AvatarError> allocationTracker, Textures texturesComponent, Materials materialsComponent, @Nullable Molang molangState, @Nullable VanillaRendering vanilla) throws AvatarError {
        super(name, module, materials, allocationTracker, texturesComponent, materialsComponent, molangState, vanilla);
        this.itemTransforms = new HashMap<>();
        int[] size = new int[] { SIZE_ESTIMATE };
        ItemRenderContext.CONTEXTS_BY_NAME.forEach((contextName, context) -> {
            @Nullable ModuleMaterials.ItemPartTransform transform = transforms.get(contextName);
            boolean flip = context.mirrorPlacement; // Save mirroring
            while (transform == null) { // If there's no transform, check fallback
                if (context.fallback == null) return; // If no fallback, skip this entry
                context = context.fallback;
                contextName = context.name;
                transform = transforms.get(contextName);
            }
            // We now have a non-null transform and context, so add it to the map
            float mirror = flip ? -1.0f : 1.0f;
            Matrix4f matrix = new Matrix4f()
                    .scaling(1f / 16)
                    .translation(transform.translation().mul(mirror,1,1, new Vector3f()))
                    .rotateZYX(transform.rotation().mul(1, mirror, mirror))
                    .scale(transform.scale());
            itemTransforms.put(context, matrix);
            size[0] += AllocationTracker.MAT4F_SIZE + AllocationTracker.REFERENCE_SIZE * 3;
        });
        if (allocationTracker != null)
            allocationTracker.track(this, size[0]);
    }

}
