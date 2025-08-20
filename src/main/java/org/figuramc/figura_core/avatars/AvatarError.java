package org.figuramc.figura_core.avatars;

import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

/**
 * Indicates an error that occurred with an Avatar, after it's been imported
 */
public class AvatarError extends FiguraException {

    public <Items extends TranslatableItems> AvatarError(Translatable<Items> translatable, Items items) {
        super(translatable, items);
    }

    public <Items extends TranslatableItems> AvatarError(Translatable<Items> translatable, Items items, Throwable cause) {
        super(translatable, items, cause);
    }
}
