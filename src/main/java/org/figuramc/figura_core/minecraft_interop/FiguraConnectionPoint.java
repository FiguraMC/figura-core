package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.components.*;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.minecraft_interop.game_data.GameDataProvider;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityPose;
import org.figuramc.figura_core.minecraft_interop.game_data.item.EquipmentSlot;
import org.figuramc.figura_core.minecraft_interop.game_data.item.ItemRarity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.ItemUseAction;
import org.figuramc.figura_core.minecraft_interop.render.ClientPartRenderer;
import org.figuramc.figura_core.minecraft_interop.text.MinecraftGlyphProvider;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTextureProvider;
import org.figuramc.figura_core.model.rendering.RenderData;
import org.figuramc.figura_core.model.rendering.shader.ShaderHookPoint;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.util.ReflectionUtils;
import org.figuramc.figura_core.util.enumlike.EnumLike;
import org.figuramc.figura_core.util.functional.ThrowingBiFunction;
import org.figuramc.figura_translations.FiguraTranslations;
import org.figuramc.memory_tracker.AllocationTracker;
import org.jetbrains.annotations.Nullable;

/**
 * This class acts as the center point where other mods (such as figura-client)
 * interop with figura-core.
 */
public class FiguraConnectionPoint {

    // Global instances of various singleton interfaces should be given implementations.
    public static MinecraftTextureProvider TEXTURE_PROVIDER = null;
    public static MinecraftGlyphProvider GLYPH_PROVIDER = null;
    public static ThrowingBiFunction<RenderData, @Nullable AllocationTracker<AvatarOutOfMemoryError>, ClientPartRenderer, AvatarOutOfMemoryError> PART_RENDERER_FACTORY = null;
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
                AvatarEvents.class, AvatarProfiling.class,
                CemSelfDeleter.class, CustomItems.class,
                EntityRoot.class, ExternalText.class,
                HudRoot.class,
                ManagerAccess.class, Materials.class, Molang.class,
                RenderDataHolder.class,
                Textures.class,
                VanillaRendering.class,

                LuaRuntime.class
        );
        ReflectionUtils.ensureInitialized(ShaderHookPoint.class);
        ReflectionUtils.ensureInitialized(EntityPose.class, EquipmentSlot.class, ItemRarity.class, ItemUseAction.class);

        // TODO let addons construct extra EnumLike instances here

        EnumLike.freeze(Event.class);
        EnumLike.freeze(AvatarComponent.Type.class);
        EnumLike.freeze(ShaderHookPoint.class);
        EnumLike.freeze(EntityPose.class);
        EnumLike.freeze(EquipmentSlot.class);
        EnumLike.freeze(ItemRarity.class);
        EnumLike.freeze(ItemUseAction.class);
        FiguraTranslations.registerTranslationResources("org/figuramc/figura_core/translations/");
    }

}
