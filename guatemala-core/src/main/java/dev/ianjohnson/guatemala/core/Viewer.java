package dev.ianjohnson.guatemala.core;

import java.lang.foreign.MemoryAddress;

/**
 * A function that accepts a {@link MemoryAddress} and returns a structured view of the underlying data as a type
 * {@code T}.
 *
 * @param <T> the type to view as
 */
@FunctionalInterface
public interface Viewer<T> {
    /**
     * Returns a view of {@code memoryAddress} as an instance of {@code T}.
     *
     * <p>The returned view does not own the underlying data, and no destructor will be called when the view is no
     * longer reachable. The view is only valid for as long as the underlying data lives.</p>
     *
     * @param memoryAddress the memory address to view
     * @return an instance of {@code T} as a view of the data at the given address
     */
    T view(MemoryAddress memoryAddress);
}
