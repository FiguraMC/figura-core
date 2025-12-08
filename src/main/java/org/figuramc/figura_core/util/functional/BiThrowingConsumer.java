package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface BiThrowingConsumer<T1, E1 extends Throwable, E2 extends Throwable> {
    void accept(T1 t1) throws E1, E2;
}
