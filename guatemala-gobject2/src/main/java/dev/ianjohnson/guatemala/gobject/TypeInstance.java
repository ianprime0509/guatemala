package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;

public final class TypeInstance {
    public static final MemoryLayout LAYOUT = BindingSupport.structLayout(ADDRESS.withName("g_class"));

    private TypeInstance() {}
}
