package org.figuramc.figura_core.script_languages.molang;

import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_molang.DefaultQueries;
import org.figuramc.figura_molang.MolangInstance;
import org.figuramc.figura_molang.QueryFactory;
import org.figuramc.figura_molang.ast.FunctionCall;
import org.figuramc.figura_molang.ast.Literal;
import org.figuramc.figura_molang.ast.VectorConstructor;
import org.figuramc.figura_molang.ast.vars.ContextVariable;
import org.figuramc.figura_molang.compile.MolangCompileException;
import org.figuramc.figura_molang.func.FloatFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllMolangQueries {

    private static final HashMap<String, MolangInstance.Query<? super Molang, RuntimeException>> AVATAR_QUERIES = new HashMap<>();

    // Java's type system around exceptions is cringe, so we can't use those queries normally.
    // We have to perform an unchecked cast to get the queries with a different error type.
    @SuppressWarnings("unchecked")
    public static <Err extends Throwable> Map<String, MolangInstance.Query<? super Molang, Err>> getAvatarQueries() {
        return (Map<String, MolangInstance.Query<? super Molang, Err>>) (Object) AVATAR_QUERIES;
    }

    static {
        AVATAR_QUERIES.putAll(DefaultQueries.getDefaultQueries());

        // General
        AVATAR_QUERIES.put("time", QueryFactory.fromStaticMethod("time", Molang.class, "time", 0, 1));

        // Animations
        AVATAR_QUERIES.put("anim_time", QueryFactory.fromContextVariable("anim_time", "anim_time"));

        // Text
        AVATAR_QUERIES.put("char_index", QueryFactory.fromContextVariable("char_index", "char_index"));
        AVATAR_QUERIES.put("char_count", QueryFactory.fromConstant("char_count", "char_count", 1));
        AVATAR_QUERIES.put("start_char_index", QueryFactory.fromConstant("start_char_index", "start_char_index", 1));
        // char_progress is short for (char_index - start_char_index) / char_count
        AVATAR_QUERIES.put("char_progress", ((parser, args, source, funcNameStart, funcNameEnd) -> {
            // Verify there's no args
            if (!args.isEmpty()) throw new MolangCompileException(MolangCompileException.WRONG_ARG_COUNT, "char_progress", String.valueOf(0), String.valueOf(args.size()), source, funcNameStart, funcNameEnd);
            // Fetch char count
            float[] charCountArr = parser.constants.get("char_count"); if (charCountArr == null) return new Literal(0);
            float charCount = charCountArr[0];
            // Fetch start char index
            float[] startCharIndexArr = parser.constants.get("start_char_index"); if (startCharIndexArr == null) return new Literal(0);
            float startCharIndex = startCharIndexArr[0];
            // Ensure char_index context variable exists
            int charIndexIndex = parser.contextVariables.indexOf("char_index");
            if (charIndexIndex == -1) return new Literal(0);
            // Return expr: (char_index - start_char_index) * (1 / char_count)
            float charCountInv = 1 / charCount;
            return new FunctionCall(FloatFunction.MUL_OP, List.of(
                    new FunctionCall(FloatFunction.SUB_OP, List.of(
                            new ContextVariable("char_index", charIndexIndex),
                            new Literal(startCharIndex)
                    )),
                    new Literal(charCountInv)
            ));
        }));

    }

}
