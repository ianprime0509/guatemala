package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class Value {
    private static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            JAVA_LONG.withName("g_type"),
            // We don't use the data field, but it needs to be present to get the correct layout
            MemoryLayout.paddingLayout(128).withName("data"));

    private static final MethodHandle G_VALUE_COPY =
            BindingSupport.lookup("g_value_copy", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle G_VALUE_TAKE_STRING =
            BindingSupport.lookup("g_value_take_string", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle G_VALUE_UNSET =
            BindingSupport.lookup("g_value_unset", FunctionDescriptor.ofVoid(ADDRESS));

    private final MemorySegment memorySegment;

    private Value(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static MemoryLayout getLayout() {
        return LAYOUT;
    }

    public static Value ofUninitialized() {
        return ofUninitialized(MemorySession.openImplicit());
    }

    public static Value ofUninitialized(MemorySession memorySession) {
        return wrapOwning(memorySession.allocate(LAYOUT));
    }

    public static Value of(String value) {
        Value v = Value.ofUninitialized();
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
        BindingSupport.runThrowing(() -> G_VALUE_COPY.invoke(other.getMemorySegment(), getMemorySegment()));
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public Type getType() {
        return Type.ofRaw(getMemorySegment().get(JAVA_LONG, 0));
    }

    public void set(String value) {
        BindingSupport.runThrowing(
                local -> G_VALUE_TAKE_STRING.invoke(getMemorySegment(), local.allocateUtf8String(value)));
    }

    private record UnsetAction(MemorySegment memorySegment) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_VALUE_UNSET.invoke(memorySegment));
        }
    }
}
