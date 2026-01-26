package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.model.part.PartTransform;
import org.figuramc.figura_core.model.part.RiggedHierarchy;
import org.figuramc.figura_core.script_hooks.callback.ScriptCallback;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Component that manages the vanilla entity's rendering for this avatar.
 */
public class VanillaRendering implements AvatarComponent<VanillaRendering> {

    public static final Type<VanillaRendering> TYPE = new Type<>("VANILLA_RENDERING", VanillaRendering::new);
    public Type<VanillaRendering> getType() { return TYPE; }

    private final AllocationTracker<AvatarOutOfMemoryError> allocationTracker;

    // Helpful toggle to hide all model parts from appearing, but still render them (so mimics and such are still updated)
    public boolean hideAllModelParts;

    // Keep some mappings to correlate data.
    // Mixins are expected to read this field.
    public final Map<VanillaPart, ScriptVanillaPart> vanillaPartToScriptPart = new HashMap<>();
    public final Map<String, ScriptVanillaPart> nameToScriptPart = new HashMap<>();

    // Requires a vanilla model to create it
    public VanillaRendering(Avatar<?> avatar, AvatarModules modules) throws AvatarOutOfMemoryError {
        allocationTracker = avatar.allocationTracker;
        VanillaModel model = avatar.vanillaModel;
        if (model == null) throw new IllegalStateException("Attempt to construct Avatar that has VanillaRendering component without a VanillaModel");
        model.accept((name, part) -> {
            // Skip any null parts
            if (part == null) return;
            // For each vanilla part, construct the corresponding script part, and add it to the maps.
            ScriptVanillaPart scriptPart = new ScriptVanillaPart(part, allocationTracker);
            vanillaPartToScriptPart.put(part, scriptPart);
            nameToScriptPart.put(name, scriptPart);
            // Check parent. Runnable will have already run on the parent before running on children, so it should be in the map.
            VanillaPart parent = part.parent();
            if (parent != null) vanillaPartToScriptPart.get(parent).children.put(name, scriptPart);
        });
        avatar.vanillaModel = null; // Clear
    }

    // A mirror to a VanillaPart, but accessible to scripts.
    public static class ScriptVanillaPart implements RiggedHierarchy<ScriptVanillaPart> {

        // VanillaPart to which this is linked
        public final VanillaPart part;
        // Children map
        public final Map<String, ScriptVanillaPart> children = new HashMap<>();
        // Script-provided values for how to transform the vanilla part
        public final PartTransform figuraTransform;

        public ScriptVanillaPart(VanillaPart part, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarOutOfMemoryError {
            this.part = part;
            this.figuraTransform = new PartTransform(allocationTracker);
        }

        // Script-provided booleans saying whether to cancel each vanilla transform phase:
        public boolean cancelVanillaOrigin, cancelVanillaRotation, cancelVanillaScale;

        // Stored transform values from when the part last rendered.
        // Regular "stored" values only include MC's transforms.
        // The "full" stored values include MC's transforms AND Figura's transforms.
        public final Vector3f
                storedOrigin = new Vector3f(),
                fullStoredOrigin = new Vector3f(),
                storedRotation = new Vector3f(), // Radians
                fullStoredRotation = new Vector3f(), // Radians
                storedScale = new Vector3f(1f),
                fullStoredScale = new Vector3f(1f),
                storedPosition = new Vector3f(), // Here for completion, but it's never modified by MC. Just always 0.
                fullStoredPosition = new Vector3f();

        // Callbacks which run when the minecraft part is rendered
        public final ArrayList<ScriptCallback<CallbackItem.Unit, CallbackItem.Unit>> vanillaRenderCallbacks = new ArrayList<>(0);

        // Implement RiggedHierarchy
        @Override
        public PartTransform getTransform() {
            return figuraTransform;
        }

        @Override
        public @Nullable ScriptVanillaPart getChildByName(String name) {
            return children.get(name);
        }
    }

}
