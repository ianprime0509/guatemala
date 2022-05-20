package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

public final class TypeClass {
    private static final MemoryLayout MEMORY_LAYOUT =
            MemoryLayout.structLayout(Type.getMemoryLayout().withName("g_type"));

    private TypeClass() {}

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }
}
