package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;

public final class TypeInstance {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(ADDRESS.withName("g_class"));

    private TypeInstance() {}
}
