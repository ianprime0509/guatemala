package dev.ianjohnson.guatemala.glib;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

public final class Bytes {
    private static final MethodHandle G_BYTES_GET_DATA =
            BindingSupport.lookup("g_bytes_get_data", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle G_BYTES_NEW =
            BindingSupport.lookup("g_bytes_new", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_LONG));
    private static final MethodHandle G_BYTES_REF =
            BindingSupport.lookup("g_bytes_ref", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle G_BYTES_UNREF =
            BindingSupport.lookup("g_bytes_unref", FunctionDescriptor.ofVoid(ADDRESS));

    private final MemoryAddress memoryAddress;

    private Bytes(MemoryAddress memoryAddress) {
        this.memoryAddress = memoryAddress;
    }

    public static Bytes of(byte[] data) {
        return wrapOwning(BindingSupport.callThrowing(local -> {
            MemorySegment dataArray = local.allocateArray(JAVA_BYTE, data.length);
            dataArray.copyFrom(MemorySegment.ofArray(data));
            return (MemoryAddress) G_BYTES_NEW.invoke(dataArray, data.length);
        }));
    }

    public static Bytes ofClasspathResource(Class<?> clazz, String resource) throws IOException {
        InputStream is = clazz.getResourceAsStream(resource);
        if (is == null) {
            throw new IllegalArgumentException("Classpath resource not found: " + resource);
        }
        byte[] data;
        try (is) {
            data = is.readAllBytes();
        }
        return of(data);
    }

    public static Bytes view(MemoryAddress memoryAddress) {
        return new Bytes(memoryAddress);
    }

    public static Bytes wrap(MemoryAddress memoryAddress) {
        BindingSupport.runThrowing(() -> G_BYTES_REF.invoke(memoryAddress));
        return wrapOwning(memoryAddress);
    }

    public MemoryAddress getMemoryAddress() {
        return memoryAddress;
    }

    public static Bytes wrapOwning(MemoryAddress memoryAddress) {
        Bytes bytes = view(memoryAddress);
        BindingSupport.registerCleanup(bytes, new UnrefAction(memoryAddress));
        return bytes;
    }

    public byte[] getData() {
        return BindingSupport.callThrowing(local -> {
            MemorySegment sizeOut = local.allocate(JAVA_LONG);
            MemoryAddress dataAddr = (MemoryAddress) G_BYTES_GET_DATA.invoke(getMemoryAddress(), sizeOut);
            long size = sizeOut.get(JAVA_LONG, 0);
            if (size == 0) {
                return new byte[0];
            }
            MemorySegment data = MemorySegment.ofAddress(dataAddr, size, local);
            return data.toArray(JAVA_BYTE);
        });
    }

    private record UnrefAction(MemoryAddress memoryAddress) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_BYTES_UNREF.invoke(memoryAddress));
        }
    }
}
