package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.ItemRenderContext;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.model.part.parts.CustomItemModelPart;
import org.figuramc.figura_core.model.part.parts.FiguraModelPart;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A component that handles rendering custom items instead of vanilla ones.
 */
public class CustomItems implements AvatarComponent<CustomItems> {

    public static final Type<CustomItems> TYPE = new Type<>("CUSTOM_ITEMS", CustomItems::new, Textures.TYPE, Materials.TYPE, Molang.TYPE, VanillaRendering.TYPE);
    public Type<CustomItems> getType() { return TYPE; }

    /**
     * The custom items which were determined by file names in the "items/" folder!
     */
    private final List<PartEntry> customItems;

    public CustomItems(Avatar<?> avatar, AvatarModules modules) throws AvatarInitError, AvatarOutOfMemoryError {
        // Fetch components
        Textures textures = avatar.assertComponent(Textures.TYPE);
        Materials materials = avatar.assertComponent(Materials.TYPE);
        Molang molang = avatar.assertComponent(Molang.TYPE);
        @Nullable VanillaRendering vanillaRendering = avatar.getComponent(VanillaRendering.TYPE);

        // Create the custom items
        customItems = new ArrayList<>();
        for (var mod : modules.loadTimeModules()) {
            for (var entry : mod.materials.customItemRoots().entrySet()) {
                // Convert the String pattern to a Matcher:
                String pattern = entry.getKey();
                if (pattern.isEmpty()) continue;
                Matcher matcher;
                if (pattern.charAt(0) == '$') {
                    String endsWith = pattern.substring(1);
                    matcher = new Matcher.EndsWithMatcher(endsWith);
                } else {
                    MinecraftIdentifier parsed = MinecraftIdentifier.parse(pattern, '$');
                    if (parsed == null) continue;
                    matcher = new Matcher.ExactMatcher(parsed);
                }
                // Convert the materials to a CustomItemModelPart
                RenderingRoot<CustomItemModelPart> mainPart;
                RenderingRoot<FiguraModelPart> flatPart;
                if (entry.getValue().model() != null) {
                    // Create a custom item model part (which has additional transform data)
                    CustomItemModelPart modelpart = new CustomItemModelPart(pattern, mod, entry.getValue().model().model(), entry.getValue().model().transformsByContext(), avatar.allocationTracker, textures, materials, molang, vanillaRendering);
                    mainPart = new RenderingRoot<>(modelpart, avatar.allocationTracker);
                } else mainPart = null;
                if (entry.getValue().textureIndex() != -1) {
                    // Convert the texture index to a FiguraModelPart
                    FiguraModelPart extruded = new FiguraModelPart(pattern, textures.getTexture(mod.index, entry.getValue().textureIndex()), avatar.allocationTracker);
                    // Set up transform to be item-ish, like in minecraft
                    extruded.transform.setScale(1f/16);
                    extruded.transform.setPosition(0f, 0f, 7.5f);
                    flatPart = new RenderingRoot<>(extruded, avatar.allocationTracker);
                } else flatPart = null;

                // Yield the entry
                customItems.add(new PartEntry(matcher, mainPart, flatPart));
            }
        }
        Collections.sort(customItems);

        // Track allocation
        if (avatar.allocationTracker != null) {
            // Track matchers
            for (var entry : customItems) {
                avatar.allocationTracker.track(entry, PartEntry.SIZE_ESTIMATE + switch (entry.matcher) {
                    case Matcher.ExactMatcher exact -> AllocationTracker.OBJECT_SIZE;
                    case Matcher.EndsWithMatcher endsWith -> AllocationTracker.OBJECT_SIZE + endsWith.ending.length() * AllocationTracker.CHAR_SIZE;
                });
            }
            // Track this
            avatar.allocationTracker.track(this, AllocationTracker.OBJECT_SIZE + AllocationTracker.REFERENCE_SIZE + customItems.size() * AllocationTracker.REFERENCE_SIZE);
        }

    }

    public @Nullable RenderingRoot<? extends FiguraModelPart> getModelPart(MinecraftItemStack stack, ItemRenderContext context) {
        // TODO: Once the API exists, try any custom callbacks before this for loop
        for (PartEntry entry : customItems) {
            if (!entry.matcher.matches(stack))
                continue;
            if (entry.mainPart == null)
                return entry.flatPart;
            if (entry.flatPart == null)
                return entry.mainPart;
            return context.defaultToExtrudedPng ? entry.flatPart : entry.mainPart;
        }
        return null;
    }

    private record PartEntry(Matcher matcher, @Nullable RenderingRoot<CustomItemModelPart> mainPart, @Nullable RenderingRoot<FiguraModelPart> flatPart) implements Comparable<PartEntry> {
        public static final int SIZE_ESTIMATE =
                AllocationTracker.OBJECT_SIZE
                + AllocationTracker.REFERENCE_SIZE * 3;
        @Override public int compareTo(@NotNull CustomItems.PartEntry o) {
            return this.matcher.compareTo(o.matcher);
        }
    }

    /**
     * Built-in matchers.
     * If the file name starts with $, then it's an EndsWithMatcher.
     * Otherwise, it's exact, with $ acting as the namespace separator.
     * Examples:
     * - $_sword.figmodel or .png matches anything whose id ends with "_sword"
     * - golden_axe.figmodel or .png matches specifically "minecraft:golden_axe"
     * - silly_mod$titanium_axe.figmodel or .png matches specifically "silly_mod:titanium_axe"
     */
    private sealed interface Matcher extends Comparable<Matcher> {
        boolean matches(MinecraftItemStack itemStack);

        record ExactMatcher(MinecraftIdentifier ident) implements Matcher {
            @Override
            public boolean matches(MinecraftItemStack itemStack) {
                return this.ident.equals(itemStack.getId());
            }

            @Override
            public int compareTo(@NotNull CustomItems.Matcher o) {
                return switch (o) {
                    case EndsWithMatcher endsWithMatcher -> -1; // Exact matchers come first
                    case ExactMatcher exactMatcher -> ident.compareTo(exactMatcher.ident);
                };
            }
        }
        record EndsWithMatcher(String ending) implements Matcher {
            @Override
            public boolean matches(MinecraftItemStack itemStack) {
                return itemStack.getId().name().endsWith(ending);
            }
            @Override
            public int compareTo(@NotNull CustomItems.Matcher o) {
                return switch (o) {
                    case ExactMatcher exactMatcher -> 1; // Exact matchers come first
                    case EndsWithMatcher endsWithMatcher -> {
                        // Longer matchers come first
                        int lenCompare = endsWithMatcher.ending.length() - this.ending.length();
                        if (lenCompare != 0) yield lenCompare;
                        yield this.ending.compareTo(endsWithMatcher.ending);
                    }
                };
            }
        }
    }

}
