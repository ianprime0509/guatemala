package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Grid extends Widget {
    private static final MethodHandle GTK_GRID_ATTACH = BindingSupport.lookup(
            "gtk_grid_attach", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT));
    private static final MethodHandle GTK_GRID_NEW =
            BindingSupport.lookup("gtk_grid_new", FunctionDescriptor.of(ADDRESS));

    public static final ObjectType<Class, Grid> TYPE =
            ObjectType.ofTypeGetter("gtk_grid_get_type", Class::new, Grid::new);

    protected Grid(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Grid of() {
        return TYPE.wrapInstanceOwning(BindingSupport.callThrowing(() -> (MemoryAddress) GTK_GRID_NEW.invoke()));
    }

    public void attach(Widget child, int column, int row, int width, int height) {
        BindingSupport.runThrowing(
                () -> GTK_GRID_ATTACH.invoke(getMemoryAddress(), child.getMemoryAddress(), column, row, width, height));
    }

    public static class Class extends Widget.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
