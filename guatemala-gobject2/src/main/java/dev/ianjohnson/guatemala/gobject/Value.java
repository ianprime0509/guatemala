package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.core.Flag;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.Set;

import static dev.ianjohnson.guatemala.glib.Types.GINT;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class Value {
    public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
            Type.LAYOUT.withName("g_type"),
            // We don't use the data field, but it needs to be present to get the correct layout
            MemoryLayout.paddingLayout(128).withName("data"));

    private static final MethodHandle G_VALUE_COPY =
            BindingSupport.lookup("g_value_copy", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle G_VALUE_INIT =
            BindingSupport.lookup("g_value_init", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_LONG));
    private static final MethodHandle G_VALUE_SET_INT =
            BindingSupport.lookup("g_value_set_int", FunctionDescriptor.ofVoid(ADDRESS, GINT));
    private static final MethodHandle G_VALUE_SET_OBJECT =
            BindingSupport.lookup("g_value_set_object", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle G_VALUE_SET_STRING =
            BindingSupport.lookup("g_value_set_string", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle G_VALUE_UNSET =
            BindingSupport.lookup("g_value_unset", FunctionDescriptor.ofVoid(ADDRESS));

    private final MemorySegment memorySegment;

    private Value(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static Value ofUninitialized() {
        return ofUninitialized(MemorySession.openImplicit());
    }

    public static Value ofUninitialized(MemorySession memorySession) {
        return wrapOwning(memorySession.allocate(LAYOUT));
    }

    public static Value of(int value) {
        Value v = ofUninitialized();
        v.set(value);
        return v;
    }

    public static Value of(Set<? extends Flag> flags) {
        return of(Flag.toInt(flags));
    }

    public static Value of(Object value) {
        Value v = ofUninitialized();
        v.set(value);
        return v;
    }

    public static Value of(String value) {
        Value v = ofUninitialized();
        v.set(value);
        return v;
    }

    public static Value wrap(MemorySegment memorySegment) {
        return new Value(memorySegment);
    }

    public static Value wrap(MemoryAddress memoryAddress, MemorySession memorySession) {
        return wrap(MemorySegment.ofAddress(memoryAddress, LAYOUT.byteSize(), memorySession));
    }

    public static Value wrapOwning(MemorySegment memorySegment) {
        Value value = new Value(memorySegment);
        BindingSupport.registerCleanup(value, new UnsetAction(memorySegment));
        return value;
    }

    public void copyFrom(Value other) {
        init(other.getType());
        BindingSupport.runThrowing(() -> G_VALUE_COPY.invoke(other.getMemorySegment(), getMemorySegment()));
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public Type getType() {
        return Type.ofRaw(getMemorySegment().get(JAVA_LONG, LAYOUT.byteOffset(groupElement("g_type"))));
    }

    public void init(Type type) {
        BindingSupport.runThrowing(() -> G_VALUE_INIT.invoke(getMemorySegment(), type.getRaw()));
    }

    public void set(int value) {
        init(Type.INT);
        BindingSupport.runThrowing(() -> G_VALUE_SET_INT.invoke(getMemorySegment(), value));
    }

    public void set(Object value) {
        init(Object.TYPE);
        BindingSupport.runThrowing(() -> G_VALUE_SET_OBJECT.invoke(getMemorySegment(), value.getMemoryAddress()));
    }

    public void set(String value) {
        init(Type.STRING);
        BindingSupport.runThrowing(
                local -> G_VALUE_SET_STRING.invoke(getMemorySegment(), local.allocateUtf8String(value)));
    }

    private record UnsetAction(MemorySegment memorySegment) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_VALUE_UNSET.invoke(memorySegment));
        }
    }
}
