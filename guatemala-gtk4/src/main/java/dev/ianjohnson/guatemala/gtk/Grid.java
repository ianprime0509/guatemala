package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Grid extends Widget {
    public static final MemoryLayout MEMORY_LAYOUT = GridImpl.MEMORY_LAYOUT;
    private static final MethodHandle GTK_GRID_ATTACH = BindingSupport.lookup(
            "gtk_grid_attach", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle GTK_GRID_NEW =
            BindingSupport.lookup("gtk_grid_new", FunctionDescriptor.of(ADDRESS));

    public static final ClassType<Class, Grid> TYPE = GridImpl.TYPE;

    protected Grid(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Grid of() {
        return TYPE.wrapOwning(BindingSupport.callThrowing(() -> (MemoryAddress) GTK_GRID_NEW.invoke()));
    }

    public void attach(Widget child, int column, int row, int width, int height) {
        BindingSupport.runThrowing(
                () -> GTK_GRID_ATTACH.invoke(address(), child.address(), column, row, width, height));
    }

    public static class Class extends Widget.Class {
        public static final MemoryLayout MEMORY_LAYOUT = GridImpl.Class.MEMORY_LAYOUT;

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
