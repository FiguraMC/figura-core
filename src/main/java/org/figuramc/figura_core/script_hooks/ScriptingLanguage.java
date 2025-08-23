package org.figuramc.figura_core.script_hooks;

import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.ScriptRuntimeComponent;
import org.figuramc.figura_core.data.ModuleImportingException;
import org.figuramc.figura_core.script_languages.lua.LuaRuntime;
import org.figuramc.figura_core.util.IOUtils;
import org.figuramc.figura_core.util.data_structures.DataTree;
import org.figuramc.figura_translations.Translatable;
import org.figuramc.figura_translations.TranslatableItems;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A place where we gather all scripting languages, and relevant operations for
 * each scripting language.
 */
public abstract class ScriptingLanguage {

    /**
     * Static map of all languages, keyed by name.
     * The value of "language" in Avatar metadata will be used to choose.
     */
    public static final Map<String, ScriptingLanguage> LANGUAGES = new ConcurrentHashMap<>();

    /**
     * Error message for unknown languages
     */
    public static final Translatable<TranslatableItems.Items1<String>> UNKNOWN_LANGUAGE
            = Translatable.create("figura_core.error.script.unknown_language", String.class);


    /**
     * The type of the avatar component which will handle this script language at runtime.
     */
    public final AvatarComponent.Type<? extends ScriptRuntimeComponent<?>> componentType;

    public ScriptingLanguage(String name, AvatarComponent.Type<? extends ScriptRuntimeComponent<?>> componentType) {
        this.componentType = componentType;
        if (LANGUAGES.putIfAbsent(name, this) != null)
            throw new IllegalArgumentException("Scripting language named \"" + name + "\" already exists");
    }

    /**
     * Traverse the avatar folder during the import phase,
     * and output a mapping from String -> byte[] of script files.
     */
    public abstract DataTree<String, byte[]> findScripts(Path avatarRoot) throws ModuleImportingException, IOException;

    // ----- Builtin ScriptingLanguages -----

    public static final ScriptingLanguage LUA = new ScriptingLanguage("lua", LuaRuntime.TYPE) {
        private static final Translatable<TranslatableItems.Items0> NO_MAIN
                = Translatable.create("figura_core.error.script.lua.no_main");

        @Override
        public DataTree<String, byte[]> findScripts(Path avatarRoot) throws ModuleImportingException, IOException {
            Path scriptsRoot = avatarRoot.resolve("scripts");
            // Ensure scripts/main.lua exists
            Path mainLua = scriptsRoot.resolve("main.lua");
            if (!Files.exists(mainLua))
                throw new ModuleImportingException(NO_MAIN, TranslatableItems.Items0.INSTANCE);
            // Fetch all files!
            return IOUtils.recursiveProcess(scriptsRoot, Files::readAllBytes, "lua", true, false);
        }
    };

}
