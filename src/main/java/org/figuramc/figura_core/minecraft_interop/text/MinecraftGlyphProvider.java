package org.figuramc.figura_core.minecraft_interop.text;

import org.figuramc.figura_core.model.rendering.FiguraRenderType;

public interface MinecraftGlyphProvider {

    // Default height of a line of text
    float getDefaultLineHeight();

    /**
     * Get glyph info for a particular character, given its codepoint.
     * If it's obfuscated, get some random-ish character with the same width as it.
     * TODO: We might want to cache the GlyphInfo per codepoint
     */
    GlyphInfo getGlyphInfo(int codepoint, boolean obfuscated);

    // Information for a glyph needed for us to render it
    // - The texture and UV coordinates where the glyph is
    // - The left/right/top/bottom of the glyph relative to the baseline
    // - The advance of the glyph (how far we should move to the right after drawing it), with and without bold
    record GlyphInfo(FiguraRenderType.TextureBinding textureBinding, float left, float right, float top, float bottom, float advance, float boldOffset) {

    }

}
