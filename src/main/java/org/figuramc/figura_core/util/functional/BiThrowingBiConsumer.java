package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface BiThrowingBiConsumer<T1, T2, E1 extends Throwable, E2 extends Throwable> {
    void accept(T1 t1, T2 t2) throws E1, E2;
}
