package dev.ianjohnson.guatemala.glib;

import java.lang.foreign.ValueLayout;

import static java.lang.foreign.ValueLayout.*;

public final class Types {
    public static final ValueLayout.OfByte GCHAR = JAVA_BYTE;
    public static final ValueLayout.OfByte GUCHAR = JAVA_BYTE;
    public static final ValueLayout.OfBoolean GBOOLEAN = JAVA_BOOLEAN;
    public static final ValueLayout.OfInt GINT = JAVA_INT;
    public static final ValueLayout.OfInt GUINT = JAVA_INT;
    public static final ValueLayout GLONG = JAVA_LONG;
    public static final ValueLayout GULONG = JAVA_LONG;
    public static final ValueLayout.OfLong GINT64 = JAVA_LONG;
    public static final ValueLayout.OfLong GUINT64 = JAVA_LONG;
    public static final ValueLayout.OfFloat GFLOAT = JAVA_FLOAT;
    public static final ValueLayout.OfDouble GDOUBLE = JAVA_DOUBLE;
    public static final ValueLayout GSIZE = JAVA_LONG;
    public static final ValueLayout.OfAddress GPOINTER = ADDRESS;

    private Types() {}
}
