package org.figuramc.figura_core.util.data_structures;

import org.figuramc.figura_core.util.functional.BiThrowingBiFunction;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;

public record Pair<A, B>(A a, B b) {

    public <A2, E1 extends Throwable, E2 extends Throwable> Pair<A2, B> mapA(BiThrowingBiFunction<A, B, A2, E1, E2> func) throws E1, E2 { return new Pair<>(func.apply(a, b), b); }
    public <A2, E1 extends Throwable, E2 extends Throwable> Pair<A2, B> mapA(BiThrowingFunction<A, A2, E1, E2> func) throws E1, E2 { return new Pair<>(func.apply(a), b); }

    public <B2, E1 extends Throwable, E2 extends Throwable> Pair<A, B2> mapB(BiThrowingBiFunction<A, B, B2, E1, E2> func) throws E1, E2 { return new Pair<>(a, func.apply(a, b)); }
    public <B2, E1 extends Throwable, E2 extends Throwable> Pair<A, B2> mapB(BiThrowingFunction<B, B2, E1, E2> func) throws E1, E2 { return new Pair<>(a, func.apply(b)); }
}
