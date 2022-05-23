package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.MemoryLayout;

public final class TypeInterface {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(Type.LAYOUT.withName("g_type"), Type.LAYOUT.withName("g_instance_type"));

    private TypeInterface() {}
}
