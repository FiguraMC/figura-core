package org.figuramc.figura_core.minecraft_interop.game_data.entity;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * A view into a Minecraft Entity instance, with required operations.
 */
public interface MinecraftEntity {

    // Return the kind of entity this is. Used as a key for CEM.
    EntityKind getKind();

    // Get the UUID of this entity.
    UUID getUUID();

    // Get the vanilla model instance for this entity.
    // Generally this should be shared between all entities of the same Kind, though this is not necessary.
    VanillaModel getModel();

    // Whether this entity is "gone" and we should stop referring to it.
    boolean isGone();

    // Various getters for script purposes.
    Vector3d getPosition(float tickDelta, Vector3d output); // Fill output with the info and return it.

}