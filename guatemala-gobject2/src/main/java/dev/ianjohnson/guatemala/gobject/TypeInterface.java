package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

public final class TypeInterface {
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            Type.LAYOUT.withName("g_type"), Type.LAYOUT.withName("g_instance_type"));

    private TypeInterface() {}
}
