package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import dev.ianjohnson.guatemala.processor.ClassNames;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.w3c.dom.Element;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.VaList;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

public record GirType(GirName name, String cType) implements GirAnyType {
    private static final Map<String, String> CANONICAL_C_TYPES = Map.ofEntries(
            entry("char", "gchar"),
            entry("unsigned char", "guchar"),
            entry("boolean", "gboolean"),
            entry("short", "gshort"),
            entry("unsigned short", "gushort"),
            entry("int", "gint"),
            entry("unsigned int", "guint"),
            entry("long", "glong"),
            entry("unsigned long", "gulong"),
            entry("float", "gfloat"),
            entry("double", "gdouble"),
            entry("size_t", "gsize"),
            entry("void*", "gpointer"),
            entry("const void*", "gconstpointer"));

    public static final GirType GCHAR = primitive("gchar");
    public static final GirType GUCHAR = primitive("guchar");
    public static final GirType GINT8 = primitive("gint8");
    public static final GirType GUINT8 = primitive("guint8");
    public static final GirType GBOOLEAN = primitive("gboolean");
    public static final GirType GUNICHAR = primitive("gunichar");
    public static final GirType GUNICHAR2 = primitive("gunichar2");
    public static final GirType GSHORT = primitive("gshort");
    public static final GirType GUSHORT = primitive("gushort");
    public static final GirType GINT16 = primitive("gint16");
    public static final GirType GUINT16 = primitive("guint16");
    public static final GirType GINT = primitive("gint");
    public static final GirType GUINT = primitive("guint");
    public static final GirType GINT32 = primitive("gint32");
    public static final GirType GUINT32 = primitive("guint32");
    public static final GirType GLONG = primitive("glong");
    public static final GirType GULONG = primitive("gulong");
    public static final GirType GINT64 = primitive("gint64");
    public static final GirType GUINT64 = primitive("guint64");
    public static final GirType GFLOAT = primitive("gfloat");
    public static final GirType GDOUBLE = primitive("gdouble");
    public static final GirType GSIZE = primitive("gsize");
    public static final GirType GSSIZE = primitive("gssize");
    public static final GirType GPOINTER = primitive("gpointer");
    public static final GirType GCONSTPOINTER = primitive("gconstpointer");
    public static final GirType VA_LIST = primitive("va_list");
    /** Fake type to represent a void return value */
    public static final GirType VOID = new GirType(new GirName(null, "none"), "void");
    /** Fake type to represent varargs; not really part of glib */
    public static final GirType VARARGS = primitive("varargs");

    public static final GirType GTYPE = new GirType(new GirName("GObject", "Type"), "GType");

    private static final Map<String, Type> BINDING_TYPES = Map.ofEntries(
            entry(GCHAR.cType(), byte.class),
            entry(GUCHAR.cType(), byte.class),
            entry(GINT8.cType(), byte.class),
            entry(GUINT8.cType(), byte.class),
            entry(GBOOLEAN.cType(), boolean.class),
            entry(GUNICHAR2.cType(), char.class),
            entry(GSHORT.cType(), short.class),
            entry(GUSHORT.cType(), short.class),
            entry(GINT16.cType(), short.class),
            entry(GUINT16.cType(), short.class),
            entry(GINT.cType(), int.class),
            entry(GUINT.cType(), int.class),
            entry(GINT32.cType(), int.class),
            entry(GUINT32.cType(), int.class),
            entry(GUNICHAR.cType(), int.class),
            entry(GLONG.cType(), long.class),
            entry(GULONG.cType(), long.class),
            entry(GINT64.cType(), long.class),
            entry(GUINT64.cType(), long.class),
            entry(GSIZE.cType(), long.class),
            entry(GSSIZE.cType(), long.class),
            entry(GTYPE.cType(), long.class),
            entry(GFLOAT.cType(), float.class),
            entry(GDOUBLE.cType(), double.class),
            entry(GPOINTER.cType(), MemoryAddress.class),
            entry(GCONSTPOINTER.cType(), MemoryAddress.class),
            entry(VOID.cType(), void.class),
            entry(VA_LIST.cType(), VaList.class));

    public GirType {
        // Special case or just mis-generated GIR?
        if ("GType".equals(name.local())) {
            name = new GirName("GObject", "Type");
        }
        // Sigh
        if ("long double".equals(name.local())) {
            name = new GirName(null, "long_double");
        }

        String canonicalCType = CANONICAL_C_TYPES.get(cType);
        if (canonicalCType != null) {
            cType = canonicalCType;
        }
    }

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

    @Override
    public Type impl(CodegenContext ctx) {
        Type binding = BINDING_TYPES.get(cType);
        if (binding != null) {
            return binding;
        }

        if (isPointer()) {
            return MemoryAddress.class;
        }

        if (name().ns() == null) {
            // TODO: maybe print a warning here
            return MemorySegment.class;
        }
        GirNamespace ns = ctx.namespaces().get(name().ns());
        if (ns == null) {
            return MemorySegment.class;
        }
        if (ns.bitFields().containsKey(name().local()) || ns.enums().containsKey(name().local())) {
            // Bit fields and enums are always internally ints
            return int.class;
        }
        if (ns.callbacks().containsKey(name().local())) {
            return MemoryAddress.class;
        }
        GirAlias alias = ns.aliases().get(name().local());
        if (alias != null) {
            return alias.type().impl(ctx);
        }
        return MemorySegment.class;
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        if (GirType.VA_LIST.cType().equals(cType()) || isPointer()) {
            return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
        } else if (isPrimitive()) {
            return CodeBlock.of("$T.$N", ClassNames.Types, name().local().toUpperCase(Locale.ROOT));
        } else {
            ClassName className = name().className(ctx);
            GirNamespace ns = ctx.namespaces().get(name().ns());
            if (ns != null && ns.classRecords().containsKey(name().local())) {
                String baseTypeName = ns.classRecords().get(name().local()).associatedClassName();
                className = ClassName.get(className.packageName(), baseTypeName, "Class");
            }
            return CodeBlock.of("$T.MEMORY_LAYOUT", className);
        }
    }

    public boolean isPointer() {
        return GPOINTER.cType.equals(cType) || GCONSTPOINTER.cType.equals(cType) || cType.endsWith("*");
    }

    public boolean isPrimitive() {
        return name.ns() == null;
    }
}
