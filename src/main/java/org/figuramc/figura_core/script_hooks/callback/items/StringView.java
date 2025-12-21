package org.figuramc.figura_core.script_hooks.callback.items;

import org.jetbrains.annotations.Nullable;

// Adds additional operations for length and copying out to a java String
public abstract non-sealed class StringView extends AbstractView implements CallbackItem {
    public abstract /* synchronized */ int length(); // Length of the string, or -1 if revoked
    public abstract /* synchronized */ @Nullable String copy(); // Copy out the string, or null if revoked
}
