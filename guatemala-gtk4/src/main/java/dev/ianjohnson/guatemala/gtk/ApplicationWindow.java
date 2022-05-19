package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.Type;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class ApplicationWindow extends Window {
    private static final MethodHandle GTK_APPLICATION_WINDOW_GET_TYPE =
            BindingSupport.lookup("gtk_application_window_get_type", FunctionDescriptor.of(JAVA_LONG));
    private static final MethodHandle GTK_APPLICATION_WINDOW_NEW =
            BindingSupport.lookup("gtk_application_window_new", FunctionDescriptor.of(ADDRESS, ADDRESS));

    private static final Type TYPE = Type.ofMethodHandle(GTK_APPLICATION_WINDOW_GET_TYPE);

    protected ApplicationWindow(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Type getType() {
        return TYPE;
    }

    public static ApplicationWindow of(Application application) {
        return newWithOwnership(ApplicationWindow::new, () ->
                (MemoryAddress) GTK_APPLICATION_WINDOW_NEW.invoke(application.getMemoryAddress()));
    }

    public static ApplicationWindow ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, ApplicationWindow::new);
    }

    public static class Class extends Window.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
