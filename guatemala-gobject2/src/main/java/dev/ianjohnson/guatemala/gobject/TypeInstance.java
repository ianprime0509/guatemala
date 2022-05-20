package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;

public final class TypeInstance {
    private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(ADDRESS.withName("g_class"));

    private TypeInstance() {}

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }
}
