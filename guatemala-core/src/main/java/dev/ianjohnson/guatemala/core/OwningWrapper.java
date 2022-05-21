package dev.ianjohnson.guatemala.core;

import java.lang.foreign.MemoryAddress;

public interface OwningWrapper<T> {
    T wrapOwning(MemoryAddress memoryAddress);
}
