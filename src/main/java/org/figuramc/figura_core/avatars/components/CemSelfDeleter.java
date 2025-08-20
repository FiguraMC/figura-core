package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarModules;

/**
 * Unloads the avatar when its entity is gone.
 * This prevents memory leaks for CEM avatars when their corresponding entity dies or is otherwise removed.
 */
public class CemSelfDeleter implements AvatarComponent<CemSelfDeleter> {

    private final Avatar<?> self;
    private final EntityUser entityUser;

    public static final Type<CemSelfDeleter> TYPE = new Type<>(CemSelfDeleter::new, EntityUser.TYPE);
    public Type<CemSelfDeleter> getType() { return TYPE; }

    public CemSelfDeleter(Avatar<?> avatar, AvatarModules modules) {
        this.self = avatar;
        this.entityUser = avatar.assertComponent(EntityUser.TYPE);
    }

    @Override
    public void tick() {
        // If entity is gone, delete this avatar.
        if (entityUser.getEntity() == null) self.unload();
    }
}
