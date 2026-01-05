package org.figuramc.figura_core.avatars.errors;

import org.figuramc.figura_core.util.exception.FiguraException;
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
}
