package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.util.exception.FiguraException;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for fetching various file paths that Figura cares about.
 */
public interface PathProvider {

    /**
     * Search the instance and provide us with a path to the Figura folder, designated by the user.
     * If none is known, prompt the user for it asynchronously.
     * If the user declines the prompt or something like that, complete the future with a CouldNotLocateFolderError.
     */
    CompletableFuture<File> locateFiguraFolder();

    // Helpers to get sub-folders without having magic strings everywhere
    default CompletableFuture<File> getAvatarsFolder() { return locateFiguraFolder().thenApply(f -> new File(f, "avatars")); }
    default CompletableFuture<File> getCEMFolder() { return locateFiguraFolder().thenApply(f -> new File(f, "cem")); }
    default CompletableFuture<File> getCommonModulesFolder() { return locateFiguraFolder().thenApply(f -> new File(f, "common_modules")); }
    default CompletableFuture<File> getGuisFolder() { return locateFiguraFolder().thenApply(f -> new File(f, "guis")); }

    // Exception for the CompletableFuture to finish with if necessary
    class CouldNotLocateFolderError extends FiguraException {
        private static final Translatable<TranslatableItems.Items0> NO_FIGURA_DIR = Translatable.create("figura_core.error.could_not_locate_folder");
        public CouldNotLocateFolderError(Throwable cause) {
            super(NO_FIGURA_DIR, TranslatableItems.Items0.INSTANCE, cause);
        }
    }

}
