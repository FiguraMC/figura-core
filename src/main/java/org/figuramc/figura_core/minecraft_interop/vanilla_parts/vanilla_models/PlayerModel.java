package org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.util.functional.ThrowingBiConsumer;
import org.jetbrains.annotations.Nullable;

/**
 * Provider for the player model
 */
public interface PlayerModel extends CapeModel, ElytraModel {

    @Nullable VanillaPart jacket(); // Child of body()
    @Nullable VanillaPart left_sleeve(); // Child of left_arm()
    @Nullable VanillaPart right_sleeve(); // Child of right_arm()
    @Nullable VanillaPart left_pants(); // Child of left_leg()
    @Nullable VanillaPart right_pants(); // Child of right_leg()

    @Override
    default <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E {
        CapeModel.super.accept(visitor);
        ElytraModel.super.accept(visitor);
        visitor.accept("jacket", jacket());
        visitor.accept("left_sleeve", left_sleeve());
        visitor.accept("right_sleeve", right_sleeve());
        visitor.accept("left_pants", left_pants());
        visitor.accept("right_pants", right_pants());
    }
}
