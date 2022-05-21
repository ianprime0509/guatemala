package dev.ianjohnson.guatemala.core;

import java.lang.foreign.MemoryAddress;

@FunctionalInterface
public interface Wrapper<T> {
    T wrap(MemoryAddress memoryAddress);
}
