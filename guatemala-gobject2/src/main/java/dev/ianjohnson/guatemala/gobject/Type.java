package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

import static dev.ianjohnson.guatemala.glib.Types.GSIZE;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public sealed class Type permits ObjectType {
    public static final ValueLayout LAYOUT = GSIZE;

    public static final Type INVALID = ofFundamental(0);
    public static final Type NONE = ofFundamental(1);
    public static final Type INTERFACE = ofFundamental(2);
    public static final Type CHAR = ofFundamental(3);
    public static final Type UCHAR = ofFundamental(4);
    public static final Type BOOLEAN = ofFundamental(5);
    public static final Type INT = ofFundamental(6);
    public static final Type UINT = ofFundamental(7);
    public static final Type LONG = ofFundamental(8);
    public static final Type ULONG = ofFundamental(9);
    public static final Type INT64 = ofFundamental(10);
    public static final Type UINT64 = ofFundamental(11);
    public static final Type ENUM = ofFundamental(12);
    public static final Type FLAGS = ofFundamental(13);
    public static final Type FLOAT = ofFundamental(14);
    public static final Type DOUBLE = ofFundamental(15);
    public static final Type STRING = ofFundamental(16);
    public static final Type POINTER = ofFundamental(17);
    public static final Type BOXED = ofFundamental(18);
    public static final Type PARAM = ofFundamental(19);
    public static final Type VARIANT = ofFundamental(21);

    private static final MethodHandle G_TYPE_QUERY =
            BindingSupport.lookup("g_type_query", FunctionDescriptor.ofVoid(JAVA_LONG, ADDRESS));

    private final long raw;

    protected Type(long raw) {
        this.raw = raw;
    }

    public static Type ofRaw(long raw) {
        return new Type(raw);
    }

    private static Type ofFundamental(long n) {
        return ofRaw(n << 2);
    }

    public long getRaw() {
        return raw;
    }

    public boolean isValid() {
        return raw != 0;
    }

    public Optional<TypeQuery> query() {
        TypeQuery typeQuery = TypeQuery.ofUninitialized();
        BindingSupport.runThrowing(() -> G_TYPE_QUERY.invoke(getRaw(), typeQuery.getMemorySegment()));
        return Optional.of(typeQuery).filter(q -> q.getType().isValid());
    }

    @Override
    public final boolean equals(java.lang.Object obj) {
        return obj instanceof Type other && other.raw == raw;
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(raw);
    }

    @Override
    public String toString() {
        return "Type(" + Long.toUnsignedString(raw) + ")";
    }
}
