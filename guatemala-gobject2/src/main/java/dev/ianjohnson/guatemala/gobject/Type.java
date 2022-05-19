package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;

public final class Type {
    private static final MethodHandle G_TYPE_REGISTER_STATIC_SIMPLE = BindingSupport.lookup(
            "g_type_register_static_simple",
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT));
    private static final MethodHandle G_TYPE_QUERY =
            BindingSupport.lookup("g_type_query", FunctionDescriptor.ofVoid(JAVA_LONG, ADDRESS));

    private final long raw;

    private Type(long raw) {
        this.raw = raw;
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
        return BindingSupport.callThrowing(local -> {
            MethodHandle consumerAccept = MethodHandles.lookup()
                    .findVirtual(Consumer.class, "accept", MethodType.methodType(void.class, java.lang.Object.class));
            Consumer<MemoryAddress> classInitRaw = a -> classInitFunc.initClass(classConstructor.apply(a));
            MemorySegment classInitUpcall = BindingSupport.upcallStub(
                    consumerAccept.bindTo(classInitRaw), FunctionDescriptor.of(ADDRESS), MemorySession.global());
            Consumer<MemoryAddress> instanceInitRaw = a -> instanceInitFunc.initInstance(instanceConstructor.apply(a));
            MemorySegment instanceInitUpcall = BindingSupport.upcallStub(
                    consumerAccept.bindTo(instanceInitRaw), FunctionDescriptor.of(ADDRESS), MemorySession.global());
            return ofRaw((long) G_TYPE_REGISTER_STATIC_SIMPLE.invoke(
                    parentType.getRaw(),
                    local.allocateUtf8String(typeName),
                    parentTypeInfo.getClassSize(),
                    classInitUpcall,
                    parentTypeInfo.getInstanceSize(),
                    instanceInitUpcall,
                    TypeFlag.toInt(typeFlags)));
        });
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
    }

    public interface InstanceInitFunc<T extends Object> {
        void initInstance(T instance);
    }
}
