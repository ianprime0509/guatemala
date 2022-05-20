package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

public final class TypeInterface {
    private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(
            Type.getMemoryLayout().withName("g_type"), Type.getMemoryLayout().withName("g_instance_type"));

    private TypeInterface() {}

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }
}
