package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static dev.ianjohnson.guatemala.glib.Types.GSIZE;
import static java.lang.foreign.ValueLayout.*;

public class Object {
    public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
            TypeInstance.LAYOUT.withName("g_type_instance"), JAVA_INT.withName("ref_count"), ADDRESS.withName("qdata"));

    private static final MethodHandle G_OBJECT_NEW_WITH_PROPERTIES = BindingSupport.lookup(
            "g_object_new_with_properties", FunctionDescriptor.of(ADDRESS, JAVA_LONG, JAVA_INT, ADDRESS, ADDRESS));
    private static final MethodHandle G_SIGNAL_CONNECT_DATA = BindingSupport.lookup(
            "g_signal_connect_data",
            FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, JAVA_INT));

    public static final ObjectType<Class, Object> TYPE = ObjectType.ofRaw(20 << 2, Class::new, Object::new);

    private final MemoryAddress memoryAddress;

    protected Object(MemoryAddress memoryAddress) {
        this.memoryAddress = Objects.requireNonNull(memoryAddress, "memoryAddress");
    }

    public static <T extends Object> T newWithProperties(ObjectType<?, T> type, Map<String, Value> properties) {
        MemoryAddress memoryAddress = BindingSupport.callThrowing(local -> {
            MemorySegment names = local.allocateArray(ADDRESS, properties.size());
            MemorySegment values = local.allocateArray(Value.LAYOUT, properties.size());
            int i = 0;
            for (var entry : properties.entrySet()) {
                names.setAtIndex(ADDRESS, i, local.allocateUtf8String(entry.getKey()));
                Value.wrap(values.asSlice(i * Value.LAYOUT.byteSize(), Value.LAYOUT.byteSize()))
                        .copyFrom(entry.getValue());
                i++;
            }
            return (MemoryAddress) G_OBJECT_NEW_WITH_PROPERTIES.invoke(type.getRaw(), properties.size(), names, values);
        });
        return type.wrapInstanceOwning(memoryAddress);
    }

    public final MemoryAddress getMemoryAddress() {
        return memoryAddress;
    }

    public final <T extends Object> T cast(ObjectType<?, T> type) {
        return type.wrapInstance(getMemoryAddress());
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

    public static class Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                TypeClass.LAYOUT.withName("g_type_class"),
                ADDRESS.withName("construct_properties"),
                ADDRESS.withName("constructor"),
                ADDRESS.withName("set_property"),
                ADDRESS.withName("get_property"),
                ADDRESS.withName("dispose"),
                ADDRESS.withName("finalize"),
                ADDRESS.withName("dispatch_properties_changed"),
                ADDRESS.withName("notify"),
                ADDRESS.withName("constructed"),
                GSIZE.withName("flags"),
                MemoryLayout.sequenceLayout(6, GPOINTER).withName("pdummy"));

        private final MemoryAddress memoryAddress;

        protected Class(MemoryAddress memoryAddress) {
            this.memoryAddress = Objects.requireNonNull(memoryAddress, "memoryAddress");
        }

        public MemoryAddress getMemoryAddress() {
            return memoryAddress;
        }
    }
}
