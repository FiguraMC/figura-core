package org.figuramc.figura_core.minecraft_interop.text;

import org.figuramc.figura_core.model.rendering.FiguraRenderType;

public interface MinecraftGlyphProvider {

    // Default height of a line of text
    float getDefaultLineHeight();

    /**
     * Get glyph info for a particular character, given its codepoint.
     * If it's obfuscated, get some random-ish character with the same width as it.
     * Might want to cache the GlyphInfo for various codepoints!
     */
    GlyphInfo getGlyphInfo(int codepoint, boolean obfuscated);

    // Information for a glyph needed for us to render it
    record GlyphInfo(FiguraRenderType.TextureBinding textureBinding, float width, float height) {

    }

}
