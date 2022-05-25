package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public final class ScrolledWindow extends Widget {
    public static final ObjectType<Class, ScrolledWindow> TYPE =
            ObjectType.ofTypeGetter("gtk_scrolled_window_get_type", Class::new, ScrolledWindow::new);

    private static final MethodHandle GTK_SCROLLED_WINDOW_NEW =
            BindingSupport.lookup("gtk_scrolled_window_new", FunctionDescriptor.of(ADDRESS));
    private static final MethodHandle GTK_SCROLLED_WINDOW_SET_CHILD =
            BindingSupport.lookup("gtk_scrolled_window_set_child", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));

    private ScrolledWindow(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static ScrolledWindow of() {
        return TYPE.wrapInstanceOwning(
                BindingSupport.callThrowing(() -> (MemoryAddress) GTK_SCROLLED_WINDOW_NEW.invoke()));
    }

    public void setChild(Widget child) {
        BindingSupport.runThrowing(
                () -> GTK_SCROLLED_WINDOW_SET_CHILD.invoke(getMemoryAddress(), child.getMemoryAddress()));
    }

    public static final class Class extends Widget.Class {
        private Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
