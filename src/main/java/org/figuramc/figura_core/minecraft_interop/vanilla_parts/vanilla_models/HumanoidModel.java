package org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.util.functional.ThrowingBiConsumer;
import org.jetbrains.annotations.Nullable;

public interface HumanoidModel extends VanillaModel {

    @Nullable VanillaPart head();
    @Nullable VanillaPart hat(); // Child of head()
    @Nullable VanillaPart body();
    @Nullable VanillaPart left_arm();
    @Nullable VanillaPart right_arm();
    @Nullable VanillaPart left_leg();
    @Nullable VanillaPart right_leg();

    @Override default <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E {
        visitor.accept("head", head());
        visitor.accept("hat", hat());
        visitor.accept("body", body());
        visitor.accept("left_arm", left_arm());
        visitor.accept("right_arm", right_arm());
        visitor.accept("left_leg", left_leg());
        visitor.accept("right_leg", right_leg());
    }
}
