package dev.ianjohnson.guatemala.glib;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

import static java.lang.foreign.ValueLayout.*;

public final class Types {
    public static final ValueLayout.OfByte GCHAR = JAVA_BYTE;
    public static final ValueLayout.OfByte GUCHAR = JAVA_BYTE;
    public static final ValueLayout.OfByte GINT8 = JAVA_BYTE;
    public static final ValueLayout.OfByte GUINT8 = JAVA_BYTE;
    public static final ValueLayout.OfBoolean GBOOLEAN = JAVA_BOOLEAN;
    public static final ValueLayout.OfInt GUNICHAR = JAVA_INT;
    public static final ValueLayout.OfChar GUNICHAR2 = JAVA_CHAR;
    public static final ValueLayout.OfShort GSHORT = JAVA_SHORT;
    public static final ValueLayout.OfShort GUSHORT = JAVA_SHORT;
    public static final ValueLayout.OfShort GINT16 = JAVA_SHORT;
    public static final ValueLayout.OfShort GUINT16 = JAVA_SHORT;
    public static final ValueLayout.OfInt GINT = JAVA_INT;
    public static final ValueLayout.OfInt GUINT = JAVA_INT;
    public static final ValueLayout.OfInt GINT32 = JAVA_INT;
    public static final ValueLayout.OfInt GUINT32 = JAVA_INT;
    public static final ValueLayout GLONG = JAVA_LONG;
    public static final ValueLayout GULONG = JAVA_LONG;
    public static final ValueLayout.OfLong GINT64 = JAVA_LONG;
    public static final ValueLayout.OfLong GUINT64 = JAVA_LONG;
    public static final ValueLayout.OfFloat GFLOAT = JAVA_FLOAT;
    public static final ValueLayout.OfDouble GDOUBLE = JAVA_DOUBLE;
    public static final ValueLayout GSIZE = JAVA_LONG;
    public static final ValueLayout GSSIZE = JAVA_LONG;
    public static final ValueLayout.OfAddress GPOINTER = ADDRESS;
    public static final ValueLayout.OfAddress GCONSTPOINTER = ADDRESS;
    public static final MemoryLayout LONG_DOUBLE = MemoryLayout.paddingLayout(128);

    private Types() {}
}
