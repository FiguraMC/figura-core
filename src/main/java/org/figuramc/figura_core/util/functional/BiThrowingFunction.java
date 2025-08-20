package org.figuramc.figura_core.util.functional;

@FunctionalInterface
public interface BiThrowingFunction<T, R, E1 extends Throwable, E2 extends Throwable> {
    R apply(T t) throws E1, E2;
}
