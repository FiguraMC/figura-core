package org.figuramc.figura_core.manage;

import org.figuramc.figura_core.avatars.Avatar;

/**
 * Implement AutoCloseable so the IDE yells at you if you don't close it.
 */
public class AvatarView<Key> implements AutoCloseable {

    private Avatar<Key> avatar;

    public AvatarView(Avatar<Key> avatar) {
        AvatarManagers.AVATAR_LOCK.lock();
        this.avatar = avatar;
    }

    public Avatar<Key> get() {
        if (avatar == null) throw new IllegalStateException("Attempt to use AvatarView after it was closed");
        return avatar;
    }

    @Override
    public void close() {
        this.avatar = null;
        AvatarManagers.AVATAR_LOCK.unlock();
    }
}
