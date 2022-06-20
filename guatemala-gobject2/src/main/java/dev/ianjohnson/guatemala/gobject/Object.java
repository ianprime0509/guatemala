package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.glib.Addressable;
import dev.ianjohnson.guatemala.glib.ReferenceCounted;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class Object implements Addressable, ReferenceCounted {
    public static final MemoryLayout MEMORY_LAYOUT = ObjectImpl.MEMORY_LAYOUT;

    public static final ClassType<Class, Object> TYPE = ClassType.ofRaw(20 << 2, Class::new, Object::new);

    private final MemoryAddress address;

    protected Object(MemoryAddress address) {
        this.address = Objects.requireNonNull(address, "address");
    }

    public static <T extends Object> T newWithProperties(ClassType<?, T> type, Map<String, Value> properties) {
        MemoryAddress memoryAddress = BindingSupport.callThrowing(local -> {
            MemorySegment names = local.allocateArray(ADDRESS, properties.size());
            MemorySegment values = local.allocateArray(Value.MEMORY_LAYOUT, properties.size());
            int i = 0;
            for (var entry : properties.entrySet()) {
                names.setAtIndex(ADDRESS, i, local.allocateUtf8String(entry.getKey()));
                Value.wrap(values.asSlice(i * Value.MEMORY_LAYOUT.byteSize(), Value.MEMORY_LAYOUT.byteSize()))
                        .copyFrom(entry.getValue());
                i++;
            }
            return ObjectImpl.ofProperties(type.getRaw(), properties.size(), names, values);
        });
        return type.wrapOwning(memoryAddress);
    }

    @Override
    public final MemoryAddress address() {
        return address;
    }

    @Override
    public void ref() {
        ObjectImpl.ref(address());
    }

    @Override
    public void unref() {
        ObjectImpl.unref(address());
    }

    public final <T extends Object> T cast(ClassType<?, T> type) {
        return type.wrap(address());
    }

    public void connectSignal(String signal, MethodHandle handler, FunctionDescriptor functionDescriptor) {
        MemorySegment function = BindingSupport.upcallStub(handler, functionDescriptor, MemorySession.global());
        try (MemorySession local = MemorySession.openConfined()) {
            GObjectImpl.signalConnectData(
                    address(), local.allocateUtf8String(signal), function, MemoryAddress.NULL, MemoryAddress.NULL, 0);
        }
    }

    public static class Class implements Addressable {
        public static final MemoryLayout MEMORY_LAYOUT = ObjectImpl.Class.MEMORY_LAYOUT;

        private final MemoryAddress address;

        protected Class(MemoryAddress address) {
            this.address = Objects.requireNonNull(address, "address");
        }

        @Override
        public MemoryAddress address() {
            return address;
        }
    }
}
