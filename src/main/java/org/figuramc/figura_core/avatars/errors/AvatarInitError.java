package org.figuramc.figura_core.avatars.errors;

import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_core.util.functional.TriThrowingRunnable;
import org.figuramc.figura_core.util.functional.TriThrowingSupplier;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

/**
 * An error occurring during Avatar initialization.
 * These are distinct from AvatarError, because AvatarError be placed on an already-loaded avatar.
 * In contrast, an AvatarInitError means an Avatar should fail to be loaded at all.
 */
public class AvatarInitError extends FiguraException {

    public <Items extends TranslatableItems> AvatarInitError(Translatable<Items> translatable, Items items) {
        super(translatable, items);
    }

    public <Items extends TranslatableItems> AvatarInitError(Translatable<Items> translatable, Items items, Throwable cause) {
        super(translatable, items, cause);
    }

    // From an AvatarError that occurs at init time
    public AvatarInitError(AvatarError alreadyTranslated) {
        super(alreadyTranslated.getMessage());
    }

    public static <E1 extends Throwable, E2 extends Throwable> void wrapAvatarError(TriThrowingRunnable<E1, E2, AvatarError> runnable) throws AvatarInitError, E1, E2 {
        try {
            runnable.run();
        } catch (AvatarError toWrap) {
            throw new AvatarInitError(toWrap);
        }
    }

    public static <R, E1 extends Throwable, E2 extends Throwable> R wrapAvatarErrorFor(TriThrowingSupplier<R, E1, E2, AvatarError> supplier) throws AvatarInitError, E1, E2 {
        try {
            return supplier.get();
        } catch (AvatarError toWrap) {
            throw new AvatarInitError(toWrap);
        }
    }



}
