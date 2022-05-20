package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class InitiallyUnowned extends Object {
    private static final MemoryLayout MEMORY_LAYOUT = Object.getMemoryLayout();

    private static final MethodHandle G_INITIALLY_UNOWNED_GET_TYPE =
            BindingSupport.lookup("g_initially_unowned_get_type", FunctionDescriptor.of(JAVA_LONG));
    private static final Type TYPE = Type.ofMethodHandle(G_INITIALLY_UNOWNED_GET_TYPE);

    protected InitiallyUnowned(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }

    public static Type getType() {
        return TYPE;
    }

    public static InitiallyUnowned ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, InitiallyUnowned::new);
    }

    public static class Class extends Object.Class {
        private static final MemoryLayout MEMORY_LAYOUT = Object.Class.getMemoryLayout();

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public static MemoryLayout getMemoryLayout() {
            return MEMORY_LAYOUT;
        }
    }
}
