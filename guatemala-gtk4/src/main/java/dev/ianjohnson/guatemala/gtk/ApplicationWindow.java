package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class ApplicationWindow extends Window {
    private static final MethodHandle GTK_APPLICATION_WINDOW_NEW =
            BindingSupport.lookup("gtk_application_window_new", FunctionDescriptor.of(ADDRESS, ADDRESS));

    public static final ObjectType<Class, ApplicationWindow> TYPE =
            ObjectType.ofTypeGetter("gtk_application_window_get_type", Class::new, ApplicationWindow::new);

    protected ApplicationWindow(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static ApplicationWindow of(Application application) {
        return TYPE.wrapInstanceOwning(BindingSupport.callThrowing(
                () -> (MemoryAddress) GTK_APPLICATION_WINDOW_NEW.invoke(application.getMemoryAddress())));
    }

    public static class Class extends Window.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
