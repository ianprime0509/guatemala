package dev.ianjohnson.guatemala.glib;

import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.*;

public final class Types {
    public static final MemoryLayout GCHAR = JAVA_BYTE;
    public static final MemoryLayout GUCHAR = JAVA_BYTE;
    public static final MemoryLayout GBOOLEAN = JAVA_BOOLEAN;
    public static final MemoryLayout GINT = JAVA_INT;
    public static final MemoryLayout GUINT = JAVA_INT;
    public static final MemoryLayout GLONG = JAVA_LONG;
    public static final MemoryLayout GULONG = JAVA_LONG;
    public static final MemoryLayout GINT64 = JAVA_LONG;
    public static final MemoryLayout GUINT64 = JAVA_LONG;
    public static final MemoryLayout GFLOAT = JAVA_FLOAT;
    public static final MemoryLayout GDOUBLE = JAVA_DOUBLE;
    public static final MemoryLayout GSIZE = JAVA_LONG;
    public static final MemoryLayout GPOINTER = ADDRESS;

    private Types() {}
}
