package org.figuramc.figura_core.util.data_structures;

import org.jetbrains.annotations.Nullable;

import java.util.Stack;

// Basic wrapper around a Stack<@Nullable T> that causes peek() to give null on empty :P
// Tired of needing to check isEmpty() before peek()ing
public class NullEmptyStack<T> extends Stack<@Nullable T> {

    @Override
    public synchronized @Nullable T peek() {
        return isEmpty() ? null : super.peek();
    }

}
