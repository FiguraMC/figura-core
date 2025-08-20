package org.figuramc.figura_core.data;

import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

/**
 * An exception that occurs while importing a module (reading it from disk).
 * Indicates some kind of problem with the FigModel or other module folder structure.
 */
public class ModuleImportingException extends FiguraException {

    public <Items extends TranslatableItems> ModuleImportingException(Translatable<Items> translatable, Items items) {
        super(translatable, items);
    }

    public <Items extends TranslatableItems> ModuleImportingException(Translatable<Items> translatable, Items items, Throwable cause) {
        super(translatable, items, cause);
    }
}
