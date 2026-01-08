package org.figuramc.figura_core.minecraft_interop.game_data.entity;

import org.figuramc.figura_core.util.enumlike.EnumLike;

public class EntityPose extends EnumLike {

    public EntityPose(String name) {
        super(name);
    }

    public static final EntityPose STANDING = new EntityPose("STANDING");
    public static final EntityPose FALL_FLYING = new EntityPose("FALL_FLYING");
    public static final EntityPose SLEEPING = new EntityPose("SLEEPING");
    public static final EntityPose SWIMMING = new EntityPose("SWIMMING");
    public static final EntityPose SPIN_ATTACK = new EntityPose("SPIN_ATTACK");
    public static final EntityPose CROUCHING = new EntityPose("CROUCHING");
    public static final EntityPose LONG_JUMPING = new EntityPose("LONG_JUMPING");
    public static final EntityPose DYING = new EntityPose("DYING");
    public static final EntityPose CROAKING = new EntityPose("CROAKING");
    public static final EntityPose USING_TONGUE = new EntityPose("USING_TONGUE");
    public static final EntityPose SITTING = new EntityPose("SITTING");
    public static final EntityPose ROARING = new EntityPose("ROARING");
    public static final EntityPose SNIFFING = new EntityPose("SNIFFING");
    public static final EntityPose EMERGING = new EntityPose("EMERGING");
    public static final EntityPose DIGGING = new EntityPose("DIGGING");
    public static final EntityPose SLIDING = new EntityPose("SLIDING");
    public static final EntityPose SHOOTING = new EntityPose("SHOOTING");
    public static final EntityPose INHALING = new EntityPose("INHALING");

}
