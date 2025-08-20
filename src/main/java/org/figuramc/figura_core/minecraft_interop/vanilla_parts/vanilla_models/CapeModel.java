package org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.util.functional.ThrowingBiConsumer;
import org.jetbrains.annotations.Nullable;

public interface CapeModel extends HumanoidModel {

    @Nullable VanillaPart cape(); // Child of body()

    @Override
    default <E extends Throwable> void accept(ThrowingBiConsumer<String, @Nullable VanillaPart, E> visitor) throws E {
        HumanoidModel.super.accept(visitor);
        visitor.accept("cape", cape());
    }
}
