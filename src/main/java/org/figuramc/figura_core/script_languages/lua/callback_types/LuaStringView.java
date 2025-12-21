package org.figuramc.figura_core.script_languages.lua.callback_types;

import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;
import org.figuramc.figura_core.script_hooks.callback.items.StringView;

public class LuaStringView extends StringView {

    // Backing string. Set to null on revoke.
    private LuaString string;

    public LuaStringView(LuaString backingString) {
        this.string = backingString; // Save the string
    }

    @Override
    public boolean isRevoked() {
        return string == null;
    }

    @Override
    public synchronized void close() {
        string = null;
        super.close();
    }

    @Override
    public synchronized int length() {
        if (isRevoked()) return -1;
        return string.length();
    }

    @Override
    public synchronized String copy() {
        if (isRevoked()) return null;
        return string.toJavaStringNoAlloc();
    }
}
