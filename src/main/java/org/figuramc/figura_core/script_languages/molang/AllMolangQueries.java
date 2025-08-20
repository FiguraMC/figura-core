package org.figuramc.figura_core.script_languages.molang;

import org.figuramc.figura_core.avatars.components.Molang;
import org.figuramc.figura_molang.DefaultQueries;
import org.figuramc.figura_molang.MolangInstance;
import org.figuramc.figura_molang.QueryFactory;

import java.util.HashMap;
import java.util.Map;

public class AllMolangQueries {

    private static final HashMap<String, MolangInstance.Query<? super Molang, RuntimeException>> AVATAR_QUERIES = new HashMap<>();

    // Java's type system around exceptions is cringe, so we can't use those queries normally.
    // We have to perform an unchecked cast to get the queries with a different error type.
    @SuppressWarnings("unchecked")
    public static <Err extends Throwable> Map<String, MolangInstance.Query<? super Molang, Err>> getAllQueries() {
        return (Map<String, MolangInstance.Query<? super Molang, Err>>) (Object) AVATAR_QUERIES;
    }

    static {
        AVATAR_QUERIES.putAll(DefaultQueries.getDefaultQueries());
        AVATAR_QUERIES.put("anim_time", QueryFactory.fromActorMethod("anim_time", Molang.class, "anim_time", 0, 1));
    }


}
