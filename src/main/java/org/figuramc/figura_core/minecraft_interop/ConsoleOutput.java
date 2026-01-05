package org.figuramc.figura_core.minecraft_interop;

import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.util.exception.FiguraException;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Various forms of output to the console, to be implemented by the client.
 */
public interface ConsoleOutput {
    /**
     * Write the given message to the output device (chat, logs, etc.)
     */
    void logSimple(@Nullable Object source, String message);

    /**
     * Write the given formatted text to the output device, if supported.
     * Outputs that lack formatting should make a good-faith effort to convert it to unformatted text.
     *
     * TODO: Needs implementation
     */
    void logFormatted(FormattedText text);

    /**
     * Write the given text in a client-native format to the output device.
     * No guarantees are made that this is any specific format.
     * If the input is invalid, use fallback text or silently continue.
     * In the case of modern Minecraft, this is JSON or SNBT.
     */
    void logNativeFormatted(String formatted);

    /**
     * Write the given message at a lower priority.
     * (This usually means outputting to the game log instead of chat)
     */
    void logVerbose(@Nullable Object source, String message);

    // Report the given error (to chat, to logs, etc)
    void reportError(FiguraException figuraException);
    // Report the given unexpected error
    void reportUnexpectedError(Throwable unexpected);
}
