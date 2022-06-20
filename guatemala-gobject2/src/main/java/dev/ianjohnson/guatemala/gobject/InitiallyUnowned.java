package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;

public class InitiallyUnowned extends Object {
    public static final MemoryLayout LAYOUT = Object.MEMORY_LAYOUT;

    public static final ClassType<Class, InitiallyUnowned> TYPE =
            ClassType.ofTypeGetter("g_initially_unowned_get_type", Class::new, InitiallyUnowned::new);

    protected InitiallyUnowned(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static class Class extends Object.Class {
        public static final MemoryLayout LAYOUT = Object.Class.MEMORY_LAYOUT;

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
