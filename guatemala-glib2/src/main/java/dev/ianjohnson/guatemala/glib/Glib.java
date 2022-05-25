package dev.ianjohnson.guatemala.glib;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;

public final class Glib {
    private static final MethodHandle G_FREE = BindingSupport.lookup("g_free", FunctionDescriptor.ofVoid(GPOINTER));

    private Glib() {}

    public static void free(MemoryAddress memoryAddress) {
        BindingSupport.runThrowing(() -> G_FREE.invoke(memoryAddress));
    }
}
