package dev.ianjohnson.guatemala.glib.gobject;

import java.lang.foreign.MemoryLayout;

import static dev.ianjohnson.guatemala.glib.Types.GSIZE;

// To satisfy some strange bindings where GType is used in GLib despite actually being part of GObject
public final class Type {
    public static final MemoryLayout MEMORY_LAYOUT = GSIZE;

    private Type() {}
}
