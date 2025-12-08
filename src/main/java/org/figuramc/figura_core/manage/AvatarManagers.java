package org.figuramc.figura_core.manage;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.util.functional.ThrowingConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for dealing with various built-in AvatarManager instances.
 */
public class AvatarManagers {

    public static final AvatarManager<UUID> ENTITIES = new AvatarManager<>();
    public static final AvatarManager<GuiKind> GUIS = new AvatarManager<>();

    /**
     * Poll all managers.
     */
    public static void pollAll() {
        ENTITIES.poll();
        GUIS.poll();
    }

    public static <E extends Throwable> void forEachAvatar(ThrowingConsumer<Avatar<?>, E> consumer) throws E {
        ENTITIES.forEach(consumer);
        GUIS.forEach(consumer);
    }

    // Types of GUI avatars; only one for now.
    public enum GuiKind {
        MAIN_GUI
    }

    // Utility: Fetch the avatar for an entity, or start looking for CEM
    public static @Nullable AvatarView<UUID> tryGetEntityAvatar(MinecraftEntity entity) {
        // See if it already has an avatar. If it does, return it immediately.
        AvatarView<UUID> avatar = ENTITIES.get(entity.getUUID());
        if (avatar != null) return avatar;
        // Otherwise, launch an async task to load a CEM avatar for it, and return null.
        CemManager.launchCemTask(entity);
        return null;
    }


}
