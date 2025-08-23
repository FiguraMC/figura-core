package org.figuramc.figura_core.util.data_structures;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

// Items must not be null
public class PeekableIterator<T> implements Iterator<T> {

    private @Nullable T next; // Null if no next value
    private final Iterator<T> delegate;

    public PeekableIterator(Iterator<@NotNull T> delegate) {
        this.delegate = delegate;
        next = delegate.hasNext() ? null : delegate.next();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    // Return next peeked value, or null if no value remains
    public @Nullable T peek() {
        return next;
    }

    public boolean take(T value) {
        if (value.equals(peek())) {
            next();
            return true;
        }
        return false;
    }

    @Override
    public T next() {
        T result = next;
        next = delegate.hasNext() ? delegate.next() : null;
        return result;
    }
}
