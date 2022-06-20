package dev.ianjohnson.guatemala.glib;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class Error {
    public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
            JAVA_INT.withName("domain"), JAVA_INT.withName("code"), ADDRESS.withName("message"));

    private static final MethodHandle G_ERROR_FREE =
            BindingSupport.lookup("g_error_free", FunctionDescriptor.ofVoid(ADDRESS));

    private final MemorySegment memorySegment;

    private Error(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static Error view(MemorySegment memorySegment) {
        return new Error(memorySegment);
    }

    public static Error view(MemoryAddress memoryAddress, MemorySession memorySession) {
        return view(MemorySegment.ofAddress(memoryAddress, LAYOUT.byteSize(), memorySession));
    }

    public static Error wrapOwning(MemorySegment memorySegment) {
        Error error = new Error(memorySegment);
        BindingSupport.registerCleanup(error, new FreeAction(memorySegment));
        return error;
    }

    public static Error wrapOwning(MemoryAddress memoryAddress, MemorySession memorySession) {
        return wrapOwning(MemorySegment.ofAddress(memoryAddress, LAYOUT.byteSize(), memorySession));
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public Quark getDomain() {
        return new Quark(memorySegment.get(JAVA_INT, LAYOUT.byteOffset(groupElement("domain"))));
    }

    public int getCode() {
        return memorySegment.get(JAVA_INT, LAYOUT.byteOffset(groupElement("code")));
    }

    public java.lang.String getMessage() {
        return memorySegment
                .get(ADDRESS, LAYOUT.byteOffset(groupElement("message")))
                .getUtf8String(0);
    }

    @Override
    public java.lang.String toString() {
        return "Error(domain=" + getDomain() + ",code=" + getCode() + ",message=" + getMessage() + ")";
    }

    private record FreeAction(MemorySegment memorySegment) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_ERROR_FREE.invoke(memorySegment));
        }
    }
}
