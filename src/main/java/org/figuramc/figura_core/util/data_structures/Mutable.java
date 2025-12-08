package org.figuramc.figura_core.util.data_structures;

public class Mutable<T> {
    public T value;
    public Mutable() { this(null); }
    public Mutable(T initial) { this.value = initial; }
}
