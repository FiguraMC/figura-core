package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.util.exception.FiguraException;

/**
 * TODO completely rework error reporting to use fancy FWL text thingies!
 */
public interface ErrorReporter {
    // Report the given error (to chat, to logs, etc)
    void report(FiguraException figuraException);
    // Report the given unexpected error
    void reportUnexpected(Throwable unexpected);
}
