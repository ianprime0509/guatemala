package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static dev.ianjohnson.guatemala.glib.Types.GSIZE;
import static java.lang.foreign.ValueLayout.*;

public final class Type {
    private static final MemoryLayout MEMORY_LAYOUT = GSIZE;

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
    public static final Type OBJECT = ofFundamental(20);
    public static final Type VARIANT = ofFundamental(21);

    private static final MethodHandle G_TYPE_REGISTER_STATIC_SIMPLE = BindingSupport.lookup(
            "g_type_register_static_simple",
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle G_TYPE_QUERY =
            BindingSupport.lookup("g_type_query", FunctionDescriptor.ofVoid(JAVA_LONG, ADDRESS));

    private final long raw;

    private Type(long raw) {
        this.raw = raw;
    }

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }

    public static Type ofMethodHandle(MethodHandle typeGetter) {
        return ofRaw(BindingSupport.callThrowing(() -> (long) typeGetter.invoke()));
    }

    public static Type ofRaw(long raw) {
        return new Type(raw);
    }

    public static <C extends Object.Class, T extends Object> Type registerStatic(
            Type parentType,
            String typeName,
            Function<MemoryAddress, C> classConstructor,
            ClassInitFunc<C> classInitFunc,
            Function<MemoryAddress, T> instanceConstructor,
            InstanceInitFunc<T> instanceInitFunc,
            Set<TypeFlag> typeFlags) {
        TypeQuery parentTypeInfo = parentType
                .query()
                .orElseThrow(() -> new IllegalArgumentException("Invalid parent type: " + parentType));
        return BindingSupport.callThrowing(local -> ofRaw((long) G_TYPE_REGISTER_STATIC_SIMPLE.invoke(
                parentType.getRaw(),
                local.allocateUtf8String(typeName),
                parentTypeInfo.getClassSize(),
                ClassInitFunc.Raw.of(classConstructor, classInitFunc).toUpcallStub(),
                parentTypeInfo.getInstanceSize(),
                InstanceInitFunc.Raw.of(instanceConstructor, instanceInitFunc).toUpcallStub(),
                TypeFlag.toInt(typeFlags))));
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
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Type other && other.raw == raw;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(raw);
    }

    @Override
    public String toString() {
        return "Type(" + Long.toUnsignedString(raw) + ")";
    }

    public interface ClassInitFunc<C extends Object.Class> {
        void initClass(C clazz);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initClass", MethodType.methodType(void.class, MemoryAddress.class)));

            static <C extends Object.Class> Raw of(
                    Function<MemoryAddress, C> constructor, ClassInitFunc<C> classInitFunc) {
                return clazz -> classInitFunc.initClass(constructor.apply(clazz));
            }

            void initClass(MemoryAddress clazz);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }

    public interface InstanceInitFunc<T extends Object> {
        void initInstance(T instance);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initInstance", MethodType.methodType(void.class, MemoryAddress.class)));

            static <T extends Object> Raw of(
                    Function<MemoryAddress, T> constructor, InstanceInitFunc<T> instanceInitFunc) {
                return instance -> instanceInitFunc.initInstance(constructor.apply(instance));
            }

            void initInstance(MemoryAddress instance);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }
}
