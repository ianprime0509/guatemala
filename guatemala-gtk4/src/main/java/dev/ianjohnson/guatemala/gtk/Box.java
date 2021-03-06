package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Box extends Widget {
    private static final MethodHandle GTK_BOX_APPEND =
            BindingSupport.lookup("gtk_box_append", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle GTK_BOX_NEW =
            BindingSupport.lookup("gtk_box_new", FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT));

    public static ClassType<Class, Box> TYPE = ClassType.ofTypeGetter("gtk_box_get_type", Class::new, Box::new);

    protected Box(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Box of(Orientation orientation, int spacing) {
        return TYPE.wrapOwning(BoxImpl.of(orientation.value(), spacing));
    }

    public void append(Widget child) {
        BindingSupport.runThrowing(() -> GTK_BOX_APPEND.invoke(address(), child.address()));
    }

    public static class Class extends Widget.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
