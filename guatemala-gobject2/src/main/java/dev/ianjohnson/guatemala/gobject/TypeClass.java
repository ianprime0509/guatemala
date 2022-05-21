package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;

public final class TypeClass {
    public static final MemoryLayout LAYOUT =
            MemoryLayout.structLayout(Type.LAYOUT.withName("g_type"));

    private TypeClass() {}
}
