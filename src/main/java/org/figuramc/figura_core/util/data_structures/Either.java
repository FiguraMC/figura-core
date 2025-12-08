package org.figuramc.figura_core.util.data_structures;

public sealed interface Either<A, B> {

    static <A, B> Either<A, B> ofA(A a) { return new Left<>(a); }
    static <A, B> Either<A, B> ofB(B b) { return new Right<>(b); }

    record Left<A, B>(A value) implements Either<A, B> {}
    record Right<A, B>(B value) implements Either<A, B> {}
}
