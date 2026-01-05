package org.figuramc.figura_core.util.exception;

import org.figuramc.figura_translations.Language;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

// An exception which can be reported to chat using styled components.
// Many of the subclasses don't actually do anything except propagate the superclass constructors.
// The purpose of these subclasses is generally just to split up the exception types according to where they might appear.
public class FiguraException extends Exception {

    public static final Translatable<TranslatableItems.Items1<String>> INTERNAL_ERROR = Translatable.create("figura_core.error.internal", String.class);
    public static final Translatable<TranslatableItems.Items1<String>> LITERAL = Translatable.create("figura_core.translation.literal", String.class);


    public <Items extends TranslatableItems> FiguraException(org.figuramc.figura_translations.Translatable<Items> translatable, Items items) {
        super(translatable.translate(Language.EN_US, items)); // TODO make lang configurable :P
    }
    public <Items extends TranslatableItems> FiguraException(org.figuramc.figura_translations.Translatable<Items> translatable, Items items, Throwable cause) {
        super(translatable.translate(Language.EN_US, items), cause); // TODO make lang configurable :P
    }
    public <T> FiguraException(Translatable<TranslatableItems.Items1<T>> translatable, T item) {
        super(translatable.translate(Language.EN_US, new TranslatableItems.Items1<>(item))); // TODO make lang configurable
    }
    public <T> FiguraException(Translatable<TranslatableItems.Items1<T>> translatable, T item, Throwable cause) {
        super(translatable.translate(Language.EN_US, new TranslatableItems.Items1<>(item)), cause); // TODO make lang configurable
    }

}
