package org.figuramc.figura_core.script_hooks.callback.items;

import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A view of a Minecraft Entity, lent temporarily from Java to an Avatar's script.
 * It can be revoked to prevent memory hostage.
 * In much the same way an Avatar allocates a List then passes a View of that list to another avatar to prevent hostages,
 * java code allocates an Entity and only passes a View of it to the avatar, to prevent an avatar from taking the global java memory hostage.
 */
public final class EntityView<T extends MinecraftEntity> implements CallbackItem {

    private @Nullable T entity;

    public EntityView(@NotNull T entity) {
        this.entity = entity;
    }

    // Check if the entity view is revoked.
    public boolean isRevoked() {
        return entity == null;
    }

    // Revoke the entity view.
    public void revoke() {
        entity = null;
    }

    // Return the entity.
    // Returns null if the view was revoked.
    public @Nullable T getEntity() {
        return entity;
    }

    // Check if this entity is of the given class.
    // If it is, returns the entity cast to that type.
    // If it isn't, or the entity is revoked, returns null.
    @SuppressWarnings("unchecked")
    public <R extends MinecraftEntity> @Nullable R checkEntityType(Class<R> clazz) {
        if (clazz.isInstance(entity)) return (R) entity;
        return null;
    }

}
