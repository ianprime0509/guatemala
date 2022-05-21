package dev.ianjohnson.guatemala.glib;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.util.Objects;
import java.util.function.Function;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;

public final class List {
    public static final MemoryLayout LAYOUT =
            MemoryLayout.structLayout(GPOINTER.withName("data"), ADDRESS.withName("next"), ADDRESS.withName("prev"));

    private final MemorySegment memorySegment;

    private List(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static List wrap(MemorySegment memorySegment) {
        return new List(memorySegment);
    }

    public static List wrap(MemoryAddress memoryAddress, MemorySession memorySession) {
        return wrap(MemorySegment.ofAddress(memoryAddress, LAYOUT.byteSize(), memorySession));
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public MemoryAddress getData() {
        return memorySegment.get(GPOINTER, LAYOUT.byteOffset(groupElement("data")));
    }

    public <T> T getData(Function<MemoryAddress, ? extends T> constructor) {
        return constructor.apply(getData());
    }
}
