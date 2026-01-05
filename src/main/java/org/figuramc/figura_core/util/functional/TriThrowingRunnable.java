package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface TriThrowingRunnable<E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> {
    void run() throws E1, E2, E3;
}
