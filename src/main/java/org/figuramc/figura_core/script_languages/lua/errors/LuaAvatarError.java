package org.figuramc.figura_core.script_languages.lua.errors;

import org.figuramc.figura_cobalt.LuaUncatchableError;
import org.figuramc.figura_core.avatars.errors.AvatarError;

/**
 * Wrapping AvatarError for better type safety with getCause().
 * Just use this to rethrow an AvatarError when in a Lua context,
 * or rethrow the AvatarError cause when in a non-Lua context.
 */
public class LuaAvatarError extends LuaUncatchableError {

    public LuaAvatarError(AvatarError wrapped) { super(wrapped); }

    public AvatarError getCause() { return (AvatarError) super.getCause(); }

}
