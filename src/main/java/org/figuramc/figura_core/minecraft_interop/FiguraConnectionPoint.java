package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.components.*;
import org.figuramc.figura_core.minecraft_interop.game_data.GameDataProvider;
import org.figuramc.figura_core.minecraft_interop.render.PartRenderer;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTextureProvider;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.figura_core.model.rendering.shader.ShaderHookPoint;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.util.ReflectionUtils;
import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.figuramc.figura_core.util.functional.ThrowingFunction;
import org.figuramc.figura_translations.FiguraTranslations;

/**
 * This class acts as the center point where other mods (such as figura-client)
 * interop with figura-core.
 */
public class FiguraConnectionPoint {

    // Global instances of various singleton interfaces should be given implementations.
    public static MinecraftTextureProvider TEXTURE_PROVIDER = null;
    public static ThrowingFunction<RenderingRoot<?>, PartRenderer, AvatarError> PART_RENDERER_FACTORY = null;
    public static GameDataProvider GAME_DATA_PROVIDER = null;
    public static ConsoleOutput CONSOLE_OUTPUT = null;
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

                ManagerAccess.class,
                LuaRuntime.class
        );
        ReflectionUtils.ensureInitialized(ShaderHookPoint.class);

        EnumLike.freeze(Event.class);
        EnumLike.freeze(AvatarComponent.Type.class);
        EnumLike.freeze(ShaderHookPoint.class);
        FiguraTranslations.registerTranslationResources("org/figuramc/figura_core/translations/");
    }

}
