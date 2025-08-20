package org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.util.functional.ThrowingBiConsumer;
import org.jetbrains.annotations.Nullable;

public interface ElytraModel extends VanillaModel {

    @Nullable VanillaPart left_wing();
    @Nullable VanillaPart right_wing();

    @Override
    default <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E {
        visitor.accept("left_wing", left_wing());
        visitor.accept("right_wing", right_wing());
    }
}
