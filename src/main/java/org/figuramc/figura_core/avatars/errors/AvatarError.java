package org.figuramc.figura_core.avatars.errors;

import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Language;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

/**
 * Indicates an error that occurred with an Avatar, after it's been imported
 */
public class AvatarError extends FiguraException {

    // Special AvatarError instance which should only be thrown if the avatar has already errored.
    // If this is the first error to happen to an avatar, then it's a bug.
    // If it's not the first error to happen to an avatar, then ignore it.
    public static final AvatarError ESCAPER = new AvatarError(FiguraException.INTERNAL_ERROR, "This is the Escaper instance; it should only be thrown if the avatar is already errored. This error message should not appear");

    public <Items extends TranslatableItems> AvatarError(Translatable<Items> translatable, Items items) {
        super(translatable, items);
    }

    public <Items extends TranslatableItems> AvatarError(Translatable<Items> translatable, Items items, Throwable cause) {
        super(translatable, items, cause);
    }

    public <T> AvatarError(Translatable<TranslatableItems.Items1<T>> translatable, T item) {
        super(translatable, item);
    }

    public <T> AvatarError(Translatable<TranslatableItems.Items1<T>> translatable, T item, Throwable cause) {
        super(translatable, item, cause);
    }

}
