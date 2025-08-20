package org.figuramc.figura_core.script_hooks.callback.items;

// Strings are immutable, so there is no "frozen" status like with a List.
public abstract non-sealed class StringView implements CallbackItem {

    private boolean isRevoked = false;

    public void revoke() { isRevoked = true; }
    public boolean isRevoked() { return isRevoked; }

    public abstract int length(); // Length of the string
    public abstract String copy(); // Copy out the string, or error if revoked
}
