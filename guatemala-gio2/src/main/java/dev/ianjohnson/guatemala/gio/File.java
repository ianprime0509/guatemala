package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.gobject.Object;

import java.lang.foreign.MemoryAddress;

public class File extends Object {
    protected File(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static File ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, File::new);
    }

    public static class Class extends Object.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
