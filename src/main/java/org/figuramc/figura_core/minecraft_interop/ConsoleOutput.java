package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.exception.FiguraException;

/**
 * TODO completely rework error reporting to use fancy FWL text thingies!
 */
public interface ConsoleOutput {
    /**
     * Write the given message to the output device (chat, logs, etc.)
     */
    void logSimple(String message);

    /**
     * Write the given formatted text to the output device, if supported.
     * Outputs that lack formatting should make a good-faith effort to convert it to unformatted text.
     */
    void logFormatted(FormattedText text);

    /**
     * Write the given text in a client-native format to the output device.
     * No guarantees are made that this is any specific format.
     * If the input is invalid, use fallback text or silently continue.
     * In the case of modern Minecraft, this is JSON or SNBT.
     */
    void logNativeFormatted(String formatted);

    // Report the given error (to chat, to logs, etc)
    void reportError(FiguraException figuraException);
    // Report the given unexpected error
    void reportUnexpectedError(Throwable unexpected);
}
