package dev.ianjohnson.guatemala.gobject;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;

public class InitiallyUnowned extends Object {
    public static final MemoryLayout LAYOUT = Object.LAYOUT;

    public static final ObjectType<Class, InitiallyUnowned> TYPE =
            ObjectType.ofTypeGetter("g_initially_unowned_get_type", Class::new, InitiallyUnowned::new);

    protected InitiallyUnowned(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static class Class extends Object.Class {
        public static final MemoryLayout LAYOUT = Object.Class.LAYOUT;

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
