package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface TriThrowingSupplier<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> {
    T get() throws E1, E2, E3;
}