package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.components.*;
import org.figuramc.figura_core.minecraft_interop.game_data.GameDataProvider;
import org.figuramc.figura_core.minecraft_interop.model_part_renderers.FiguraModelPartRendererFactory;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTextureProvider;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.util.ReflectionUtils;
import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.figuramc.figura_translations.FiguraTranslations;

/**
 * This class acts as the center point where other mods (including figura-client)
 * interop with figura-core.
 */
public class FiguraConnectionPoint {

    // Global instances of various singleton interfaces should be given implementations.
    public static FiguraModelPartRendererFactory MODEL_PART_RENDERER_FACTORY = null;
    public static MinecraftTextureProvider TEXTURE_PROVIDER = null;
    public static GameDataProvider GAME_DATA_PROVIDER = null;
    public static ErrorReporter ERROR_REPORTER = null;
    public static PathProvider PATH_PROVIDER = null;

    /**
     * Invoke this when you're ready for figura-core to initialize.
     * You should have registered all EnumLike instances and given values to all of the singletons.
     */
    public static void finishInit() {
        ReflectionUtils.ensureInitialized(Event.class);
        ReflectionUtils.ensureInitialized(
                CemSelfDeleter.class, CustomItems.class, EntityRoot.class,
                EntityUser.class, HudRoot.class, Molang.class,
                Textures.class, VanillaRendering.class,

                LuaRuntime.class
        );

        EnumLike.freeze(Event.class);
        EnumLike.freeze(AvatarComponent.Type.class);
        FiguraTranslations.registerTranslationResources("org/figuramc/figura_core/translations/");
    }

}
