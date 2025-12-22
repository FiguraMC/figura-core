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
public final class EntityView<T extends MinecraftEntity> extends SimpleView<T> implements CallbackItem {

    public EntityView(@NotNull T entity) { super(entity); }
    public EntityView(@NotNull T entity, AbstractView parent) { super(entity, parent); }

}
