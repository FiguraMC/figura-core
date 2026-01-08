package org.figuramc.figura_core.avatars.components;

import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.AvatarComponent;
import org.figuramc.figura_core.avatars.errors.AvatarError;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.errors.AvatarOutOfMemoryError;
import org.figuramc.figura_core.script_languages.molang.AllMolangQueries;
import org.figuramc.figura_molang.CompiledMolang;
import org.figuramc.figura_molang.MolangInstance;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Item used as the Actor for an avatar's molang states.
 * All queries supported by Figura's molang are implemented on it.
 */
public class Molang implements AvatarComponent<Molang> {

    public static final Type<Molang> TYPE = new Type<>("MOLANG", Molang::new); // Depends on entity user being updated before it
    public Type<Molang> getType() { return TYPE; }

    // The molang instance
    private final MolangInstance<Molang, AvatarOutOfMemoryError> molangInstance;

    public Molang(Avatar<?> avatar, AvatarModules modules) throws AvatarOutOfMemoryError {
        this.molangInstance = new MolangInstance<>(this, avatar.allocationTracker, AllMolangQueries.getAvatarQueries());
    }

    // Different compilation contexts and their context variables
    private static final List<String> ANIMATION_CONTEXT_VARS = List.of("anim_time");
    private static final List<String> TEXT_EXPRESSION_VARS = List.of("char_index");

    public CompiledMolang<Molang> compileAnimExpr(String source) throws MolangCompileException, AvatarOutOfMemoryError {
        return molangInstance.compile(source, ANIMATION_CONTEXT_VARS, Map.of());
    }

    public CompiledMolang<Molang> compileTextExpr(String source, int startCharIndex, int charCount) throws MolangCompileException, AvatarOutOfMemoryError {
        return molangInstance.compile(source, TEXT_EXPRESSION_VARS, Map.of(
                "start_char_index", new float[] { startCharIndex },
                "char_count", new float[] { charCount }
        ));
    }

    // Some query methods

    private static final long TIMER_START = System.nanoTime(); //
    public static float time() {
        return (float) (((double) (System.nanoTime() - TIMER_START) / 100_000L) / 10_000.0);
    }

}
