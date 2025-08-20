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

    /**
     * Whenever client code is doing anything with an Avatar, they need to obtain this global lock.
     * Avatar code cannot run on multiple threads at once, not even different Avatars on different threads,
     * since Avatars can communicate with one another.
     */
    public static final ReentrantLock AVATAR_LOCK = new ReentrantLock(true);

    public static final AvatarManager<UUID> ENTITIES = new AvatarManager<>();
    public static final AvatarManager<GuiKind> GUIS = new AvatarManager<>();

    /**
     * Poll all managers. This should always be run on the main thread!!
     */
    public static void pollAll() {
        AVATAR_LOCK.lock();
        try { ENTITIES.poll(); GUIS.poll(); } finally { AVATAR_LOCK.unlock(); }
    }

    public static <E extends Throwable> void forEachAvatar(ThrowingConsumer<Avatar<?>, E> consumer) throws E {
        AVATAR_LOCK.lock();
        try { ENTITIES.forEach(consumer); GUIS.forEach(consumer); } finally { AVATAR_LOCK.unlock(); }
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
