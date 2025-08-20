package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.animation.AnimationInstance;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.script_languages.molang.AllMolangQueries;
import org.figuramc.figura_core.util.NullEmptyStack;
import org.figuramc.figura_core.util.functional.ThrowingSupplier;
import org.figuramc.figura_molang.MolangInstance;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

/**
 * Item used as the Actor for an avatar's molang states.
 * All queries supported by Figura's molang are implemented on it.
 */
public class Molang implements AvatarComponent<Molang> {

    public static final Type<Molang> TYPE = new Type<>(Molang::new, EntityUser.TYPE); // Depends on entity user being updated before it
    public Type<Molang> getType() { return TYPE; }

    // The entity which has the avatar equipped, if any
    private final @Nullable EntityUser entityUser;
    // The stack of currently used animation instances. Normally only 1 slot is used, unless nested calls happen
    private final Stack<@Nullable AnimationInstance> animInstances = new NullEmptyStack<>();

    // The molang instance
    public final MolangInstance<Molang, AvatarError> molangInstance;

    public Molang(Avatar<?> avatar, AvatarModules modules) throws AvatarError {
        this.entityUser = avatar.getComponent(EntityUser.TYPE);
        this.molangInstance = new MolangInstance<>(this, avatar.allocationTracker, AllMolangQueries.getAllQueries());
    }

    public void pushAnim(AnimationInstance instance) { this.animInstances.push(instance); }
    public void popAnim() { this.animInstances.pop(); }

    // Run a task with the given animation instance
    public <T, E extends Throwable> T withAnim(AnimationInstance instance, ThrowingSupplier<T, E> task) throws E {
        animInstances.push(instance);
        try {
            return task.get();
        } finally {
            animInstances.pop();
        }
    }

    // Query methods
    public float anim_time() {
        AnimationInstance a = animInstances.peek(); if (a == null) return 0;
        return a.getTime();
    }

}
