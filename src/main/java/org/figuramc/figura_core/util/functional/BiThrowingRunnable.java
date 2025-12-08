package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface BiThrowingRunnable<E1 extends Throwable, E2 extends Throwable> {
    void run() throws E1, E2;
}
