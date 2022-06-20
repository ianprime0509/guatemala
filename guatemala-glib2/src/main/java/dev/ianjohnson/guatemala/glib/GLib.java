package dev.ianjohnson.guatemala.glib;

import java.lang.foreign.MemoryAddress;

public final class GLib {
    private GLib() {}

    public static void free(MemoryAddress memoryAddress) {
        GLibImpl.free(memoryAddress);
    }
}
