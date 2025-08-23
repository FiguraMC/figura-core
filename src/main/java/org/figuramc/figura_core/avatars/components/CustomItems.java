package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.ItemRenderContext;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.model.part.CustomItemModelPart;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.model.renderers.Renderable;
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

    public static final Type<CustomItems> TYPE = new Type<>(CustomItems::new, Textures.TYPE, Molang.TYPE, VanillaRendering.TYPE);
    public Type<CustomItems> getType() { return TYPE; }

    /**
     * The custom items which were determined by file names in the "items/" folder!
     */
    private final List<PartEntry> customItems;

    public CustomItems(Avatar<?> avatar, AvatarModules modules) throws AvatarError {
        // Fetch components
        Textures textures = avatar.assertComponent(Textures.TYPE);
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
                    MinecraftItem item = FiguraConnectionPoint.GAME_DATA_PROVIDER.getItem(pattern.replace('$', ':'));
                    if (item == null) continue;
                    matcher = new Matcher.ExactMatcher(item);
                }
                // Convert the materials to a CustomItemModelPart
                Renderable<CustomItemModelPart> mainPart = entry.getValue().model() != null ? new Renderable<>(new CustomItemModelPart(pattern, mod, entry.getValue().model().model(), entry.getValue().model().transformsByContext(), avatar.allocationTracker, textures, molang, vanillaRendering)) : null;
                // Convert the texture index to a FiguraModelPart
                Renderable<FiguraModelPart> flatPart = entry.getValue().textureIndex() != -1 ? new Renderable<>(new FiguraModelPart(pattern, textures.getTexture(mod.index, entry.getValue().textureIndex()), avatar.allocationTracker)) : null;
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

    public @Nullable Renderable<? extends FiguraModelPart> getModelPart(MinecraftItemStack stack, ItemRenderContext context) {
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

    private record PartEntry(Matcher matcher, @Nullable Renderable<CustomItemModelPart> mainPart, @Nullable Renderable<FiguraModelPart> flatPart) implements Comparable<PartEntry> {
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
     * - $_sword.figmodel matches anything whose id ends with "_sword"
     * - golden_axe.figmodel matches specifically "minecraft:golden_axe"
     * - silly_mod$titanium_axe.figmodel matches specifically "silly_mod:titanium_axe"
     */
    private sealed interface Matcher extends Comparable<Matcher> {
        boolean matches(MinecraftItemStack itemStack);

        record ExactMatcher(MinecraftItem item) implements Matcher {
            @Override
            public boolean matches(MinecraftItemStack itemStack) {
                return itemStack.getItem() == item;
            }

            @Override
            public int compareTo(@NotNull CustomItems.Matcher o) {
                if (o instanceof EndsWithMatcher) return -1;
                return item.identifier().compareTo(((ExactMatcher) o).item.identifier());
            }
        }
        record EndsWithMatcher(String ending) implements Matcher {
            @Override
            public boolean matches(MinecraftItemStack itemStack) {
                return itemStack.getItem().identifier().endsWith(ending);
            }
            @Override
            public int compareTo(@NotNull CustomItems.Matcher o) {
                if (o instanceof ExactMatcher) return 1;
                int lenCompare = ((EndsWithMatcher) o).ending.length() - this.ending.length();
                if (lenCompare != 0) return lenCompare;
                return this.ending.compareTo(((EndsWithMatcher) o).ending);
            }
        }
    }

}
