package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.MemoryAddress;

public class File extends Object {
    public static final ObjectType<Class, File> TYPE =
            ObjectType.ofTypeGetter("g_file_get_type", Class::new, File::new);

    protected File(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static class Class extends Object.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
