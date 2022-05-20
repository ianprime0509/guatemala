package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.*;

public final class TypeQuery {
    private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(
            JAVA_LONG.withName("type"),
            ADDRESS.withName("type_name"),
            JAVA_INT.withName("class_size"),
            JAVA_INT.withName("instance_size"));

    private final MemorySegment memorySegment;

    private TypeQuery(MemorySegment memorySegment) {
        this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
    }

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }

    public static TypeQuery ofUninitialized() {
        return ofUninitialized(MemorySession.openImplicit());
    }

    public static TypeQuery ofUninitialized(MemorySession memorySession) {
        return new TypeQuery(memorySession.allocate(MEMORY_LAYOUT));
    }

    public static TypeQuery wrap(MemorySegment memorySegment) {
        return new TypeQuery(memorySegment);
    }

    public MemorySegment getMemorySegment() {
        return memorySegment;
    }

    public Type getType() {
        return Type.ofRaw(memorySegment.get(JAVA_LONG, MEMORY_LAYOUT.byteOffset(groupElement("type"))));
    }

    public String getTypeName() {
        return memorySegment
                .get(ADDRESS, MEMORY_LAYOUT.byteOffset(groupElement("type_name")))
                .getUtf8String(0);
    }

    public int getClassSize() {
        return memorySegment.get(JAVA_INT, MEMORY_LAYOUT.byteOffset(groupElement("class_size")));
    }

    public int getInstanceSize() {
        return memorySegment.get(JAVA_INT, MEMORY_LAYOUT.byteOffset(groupElement("instance_size")));
    }
}
