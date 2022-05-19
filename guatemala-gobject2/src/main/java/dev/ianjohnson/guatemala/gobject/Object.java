package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;

public class Object {
    private static final MethodHandle G_OBJECT_NEW_WITH_PROPERTIES = BindingSupport.lookup(
            "g_object_new_with_properties", FunctionDescriptor.of(ADDRESS, JAVA_LONG, JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle G_OBJECT_REF =
            BindingSupport.lookup("g_object_ref", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle G_OBJECT_UNREF =
            BindingSupport.lookup("g_object_unref", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle G_SIGNAL_CONNECT_DATA = BindingSupport.lookup(
            "g_signal_connect_data",
            FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_INT));
    // Fundamental type, defined in gtype.h
    private static final Type TYPE = Type.ofRaw(20 << 2);

    private final MemoryAddress memoryAddress;

    protected Object(MemoryAddress memoryAddress) {
        this.memoryAddress = Objects.requireNonNull(memoryAddress, "memoryAddress");
    }

    public static Type getType() {
        return TYPE;
    }

    public static <T extends Object> T newWithProperties(
            Type type, Function<MemoryAddress, T> constructor, Map<String, Value> properties) {
        return newWithOwnership(constructor, local -> {
            MemorySegment names = local.allocateArray(ADDRESS, properties.size());
            MemorySegment values = local.allocateArray(Value.getLayout(), properties.size());
            int i = 0;
            for (var entry : properties.entrySet()) {
                names.setAtIndex(ADDRESS, i, local.allocateUtf8String(entry.getKey()));
                Value.wrap(values.getAtIndex(ADDRESS, i), local).copyFrom(entry.getValue());
                i++;
            }
            return (MemoryAddress) G_OBJECT_NEW_WITH_PROPERTIES.invoke(type.getRaw(), properties.size(), names, values);
        });
    }

    public static Object ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, Object::new);
    }

    protected static <T extends Object> T ofMemoryAddress(
            MemoryAddress memoryAddress, Function<MemoryAddress, T> constructor) {
        T obj = constructor.apply(memoryAddress);
        BindingSupport.runThrowing(() -> G_OBJECT_REF.invoke(obj.getMemoryAddress()));
        BindingSupport.registerCleanup(obj, new UnrefAction(obj.getMemoryAddress()));
        return obj;
    }

    protected static <T extends Object> T newWithOwnership(
            Function<MemoryAddress, T> constructor, BindingSupport.ThrowingCallable<MemoryAddress> addressFunc) {
        return wrapOwning(BindingSupport.callThrowing(addressFunc), constructor);
    }

    protected static <T extends Object> T newWithOwnership(
            Function<MemoryAddress, T> constructor,
            BindingSupport.ThrowingCallableWithLocal<MemoryAddress> addressFunc) {
        return wrapOwning(BindingSupport.callThrowing(addressFunc), constructor);
    }

    private static <T extends Object> T wrapOwning(
            MemoryAddress memoryAddress, Function<MemoryAddress, T> constructor) {
        T obj = constructor.apply(memoryAddress);
        BindingSupport.registerCleanup(obj, new UnrefAction(memoryAddress));
        return obj;
    }

    public MemoryAddress getMemoryAddress() {
        return memoryAddress;
    }

    public void connectSignal(String signal, MethodHandle handler, FunctionDescriptor functionDescriptor) {
        MemorySegment function = BindingSupport.upcallStub(handler, functionDescriptor, MemorySession.global());
        BindingSupport.runThrowing(local -> G_SIGNAL_CONNECT_DATA.invoke(
                getMemoryAddress(),
                local.allocateUtf8String(signal),
                function,
                MemoryAddress.NULL,
                MemoryAddress.NULL,
                0));
    }

    private record UnrefAction(MemoryAddress memoryAddress) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_OBJECT_UNREF.invoke(memoryAddress));
        }
    }

    public static class Class {
        private final MemoryAddress memoryAddress;

        protected Class(MemoryAddress memoryAddress) {
            this.memoryAddress = Objects.requireNonNull(memoryAddress, "memoryAddress");
        }

        public MemoryAddress getMemoryAddress() {
            return memoryAddress;
        }
    }
}
