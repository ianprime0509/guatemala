package dev.ianjohnson.guatemala.glib;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class Error {
    private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(
            JAVA_INT.withName("domain"), JAVA_INT.withName("code"), ADDRESS.withName("message"));

    private static final MethodHandle G_ERROR_FREE =
            BindingSupport.lookup("g_error_free", FunctionDescriptor.ofVoid(ADDRESS));

    private final MemorySegment memorySegment;

    private Error(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }

    public static Error wrap(MemorySegment memorySegment) {
        return new Error(memorySegment);
    }

    public static Error wrap(MemoryAddress memoryAddress, MemorySession memorySession) {
        return wrap(MemorySegment.ofAddress(memoryAddress, MEMORY_LAYOUT.byteSize(), memorySession));
    }

    public static Error wrapOwning(MemorySegment memorySegment) {
        Error error = new Error(memorySegment);
        BindingSupport.registerCleanup(error, new FreeAction(memorySegment));
        return error;
    }

    public static Error wrapOwning(MemoryAddress memoryAddress, MemorySession memorySession) {
        return wrapOwning(MemorySegment.ofAddress(memoryAddress, MEMORY_LAYOUT.byteSize(), memorySession));
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public Quark getDomain() {
        return Quark.ofRaw(memorySegment.get(JAVA_INT, MEMORY_LAYOUT.byteOffset(groupElement("domain"))));
    }

    public int getCode() {
        return memorySegment.get(JAVA_INT, MEMORY_LAYOUT.byteOffset(groupElement("code")));
    }

    public String getMessage() {
        return memorySegment
                .get(ADDRESS, MEMORY_LAYOUT.byteOffset(groupElement("message")))
                .getUtf8String(0);
    }

    @Override
    public String toString() {
        return "Error(domain=" + getDomain() + ",code=" + getCode() + ",message=" + getMessage() + ")";
    }

    private record FreeAction(MemorySegment memorySegment) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_ERROR_FREE.invoke(memorySegment));
        }
    }
}
