package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface BiThrowingBiFunction<T1, T2, R, E1 extends Throwable, E2 extends Throwable> {
    R apply(T1 t1, T2 t2) throws E1, E2;
}
