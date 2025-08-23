package org.figuramc.figura_core.model.shader;

import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.jetbrains.annotations.Nullable;

/**
 * Algebraic Data Type for all Figura render types.
 * Each model part has a render type and a priority for inheritance.
 * FiguraPartRenderer instances will need to figure out how to handle these.
 */
public sealed interface FiguraRenderType {

    /**
     * Basic rendering setup. Most commonly used.
     */
    record Basic(@Nullable MinecraftTexture mainTex, @Nullable MinecraftTexture emissiveTex) implements FiguraRenderType {}

    /**
     * One-off, non-customizable render types, with global INSTANCE objects.
     */
    final class EndPortal implements FiguraRenderType {
        public static final EndPortal INSTANCE = new EndPortal();
        private EndPortal() {}
    }
    final class EndGateway implements FiguraRenderType {
        public static final EndGateway INSTANCE = new EndGateway();
        private EndGateway() {}
    }

}
