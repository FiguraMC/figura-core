package org.figuramc.figura_core.model.texture;

import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.errors.AvatarInitError;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.texture.OwnedMinecraftTexture;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.figura_core.util.PngSize;
import org.figuramc.figura_translations.TranslatableItems;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FiguraTextureAtlas extends StandaloneAvatarTexture {

    protected FiguraTextureAtlas(OwnedMinecraftTexture backing) {
        super(backing);
    }

    /**
     * Create and upload the atlas.
     */
    private static FiguraTextureAtlas create(int totalWidth, int totalHeight, List<TextureRectangle> rectangles, @Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarInitError, AvatarOutOfMemoryError {
        // Create the backing texture.
        // TODO consider moving power of 2 stuff into the generation process itself?
        int width = MathUtils.smallestEncompassingPowerOfTwo(totalWidth);
        int height = MathUtils.smallestEncompassingPowerOfTwo(totalHeight);
        // TODO check size and OOM early if too big before creating it
        var atlas = FiguraConnectionPoint.TEXTURE_PROVIDER.createBlankTexture(width, height);
        if (allocationTracker != null) allocationTracker.track(atlas, 4 * width * height);

        // Add images to the backing texture
        for (TextureRectangle rectangle : rectangles) {
            OwnedMinecraftTexture tex = null;
            try {
                tex = FiguraConnectionPoint.TEXTURE_PROVIDER.createTextureFromPng(rectangle.data);
                atlas.paste(tex, rectangle.getX(), rectangle.getY(), 0, 0, tex.width(), tex.height());
            } catch (IOException invalidPng) {
                throw new AvatarInitError(AvatarTexture.INVALID_PNG, new TranslatableItems.Items1<>(rectangle.name), invalidPng);
            } finally {
                if (tex != null) tex.destroy();
                rectangle.name = null;
                rectangle.data = null;
            }
        }

        // Return instance
        return new FiguraTextureAtlas(atlas);
    }

    // The texture will be destroyed eventually. There is no need to use the returned future if you don't care when the texture is destroyed.

    // Uploads the texture to the texture manager, committing any changes that happened CPU-side.
    // This happens on the render thread, so this returns a future indicating uploading is complete.

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final List<TextureRectangle> rectangles = new ArrayList<>();

        private Builder() {}

        // Get back a TextureRectangle.
        // Later, once you call .build(), the rectangle will be updated to have
        // its "x" and "y" values set.
        public TextureRectangle insert(String texName, byte[] png) throws AvatarInitError {
            TextureRectangle rect = new TextureRectangle(texName, png);
            rectangles.add(rect);
            return rect;
        }

        /**
         * Create and upload the atlas. If there are no textures to atlas, returns null.
         */
        public @Nullable FiguraTextureAtlas build(@Nullable AllocationTracker<AvatarOutOfMemoryError> allocationTracker) throws AvatarInitError, AvatarOutOfMemoryError {
            if (rectangles.isEmpty()) return null;

            // Guess a width as sqrt(sum(rectangle areas))
            int totalArea = 0;
            for (TextureRectangle rectangle : rectangles) totalArea += rectangle.getArea();
            int widthGuess = MathUtils.smallestEncompassingPowerOfTwo((int) Math.ceil(Math.sqrt(totalArea)));

            // Sort by height; the tallest is at the end
            rectangles.sort(Comparator.comparingInt(TextureRectangle::getHeight));
            List<Region> unusedRegions = new ArrayList<>();

            int totalWidth = 0;
            int totalHeight = rectangles.getLast().getHeight();
            int currentX = 0;
            int currentY = 0;

            for (int index = rectangles.size() - 1; index >= 0; index--) {
                TextureRectangle rect = rectangles.get(index);
                // Search for the shortest unused region which can fit the rect:
                int bestUnusedRegionIndex = ListUtils.filteredIndexOfMinimal(unusedRegions, region -> region.canFit(rect), Region::height);
                if (bestUnusedRegionIndex == -1) {
                    // No region can fit this rect, so let's add it as we go
                    if (currentX + rect.width > widthGuess) {
                        // Wrap to the next line
                        rect.x = 0;
                        rect.y = totalHeight;
                        currentX = rect.width;
                        currentY = totalHeight;
                        totalHeight += rect.height;
                    } else {
                        // Add this horizontally
                        rect.x = currentX;
                        rect.y = currentY;
                        currentX += rect.width;
                        totalWidth = Math.max(totalWidth, currentX);
                        // Add an unused region below it if needed
                        if (totalHeight - rect.getHeight() != currentY)
                            unusedRegions.add(new Region(rect.getX(), rect.getBottom(), rect.getWidth(), totalHeight - rect.getHeight()));
                    }
                } else {
                    // There's an unused region that can fit this rect, so let's use it:
                    Region region = unusedRegions.remove(bestUnusedRegionIndex);
                    rect.x = region.x;
                    rect.y = region.y;
                    if (rect.width != region.width)
                        unusedRegions.add(new Region(rect.getRight(), rect.getY(), region.width - rect.width, region.height));
                    if (rect.height != region.height)
                        unusedRegions.add(new Region(rect.getX(), rect.getBottom(), rect.width, region.height - rect.height));
                }
            }

            return FiguraTextureAtlas.create(totalWidth, totalHeight, rectangles, allocationTracker);
        }

    }

    // Failed to parse texture "%s" - are you sure it's a valid PNG file?


    public static class TextureRectangle {
        private String name;
        private byte[] data;
        private int x = -1, y = -1;
        private final int width, height;

        private TextureRectangle(String texName, byte[] png) throws AvatarInitError {
            try {
                this.name = texName;
                PngSize size = PngSize.fromByteArray(png);
                if (size.width() < 1 || size.height() < 1) {
                    throw new AvatarInitError(AvatarTexture.INVALID_PNG, new TranslatableItems.Items1<>(texName));
                }
                this.width = size.width();
                this.height = size.height();
                this.data = png;
            } catch (IOException ioException) {
                throw new AvatarInitError(AvatarTexture.INVALID_PNG, new TranslatableItems.Items1<>(texName));
            }
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getMaxSide() { return Math.max(width, height); }
        public int getArea() { return width * height; }
        public int getRight() { return x + width; }
        public int getBottom() { return y + height; }

        boolean contains(int x, int y) { return x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height; }
    }

    private record Region(int x, int y, int width, int height) {
        boolean canFit(TextureRectangle rectangle) {
            return rectangle.width <= this.width && rectangle.height <= this.height;
        }
    }
}
