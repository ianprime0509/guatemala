package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirType(GirName name, String cType) implements GirAnyType {
    public static final GirType GCHAR = primitive("gchar");
    public static final GirType GUCHAR = primitive("guchar");
    public static final GirType GBOOLEAN = primitive("gboolean");
    public static final GirType GINT = primitive("gint");
    public static final GirType GUINT = primitive("guint");
    public static final GirType GLONG = primitive("glong");
    public static final GirType GULONG = primitive("gulong");
    public static final GirType GINT64 = primitive("gint64");
    public static final GirType GUINT64 = primitive("guint64");
    public static final GirType GFLOAT = primitive("gfloat");
    public static final GirType GDOUBLE = primitive("gdouble");
    public static final GirType GSIZE = primitive("gzize");
    public static final GirType GPOINTER = primitive("gpointer");
    public static final GirType GCONSTPOINTER = primitive("gconstpointer");

    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "type".equals(element.getLocalName());
    }

    public static GirType load(Element element, String ns) {
        GirName name = GirName.parse(element.getAttributeNS(null, "name"), ns);
        String cType = element.getAttributeNS(NS.C, "type");
        return new GirType(name, cType);
    }

    private static GirType primitive(String name) {
        return new GirType(new GirName(null, name), name);
    }

    public boolean isClass() {
        return name.local().endsWith("Class");
    }

    public boolean isPointer() {
        return cType.endsWith("*");
    }

    public boolean isPrimitive() {
        return name.ns() == null;
    }
}
