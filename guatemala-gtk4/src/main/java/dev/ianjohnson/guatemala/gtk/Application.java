package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.core.BitField;
import dev.ianjohnson.guatemala.gio.ApplicationFlags;
import dev.ianjohnson.guatemala.glib.List;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySession;
import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.Set;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Application extends dev.ianjohnson.guatemala.gio.Application {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(dev.ianjohnson.guatemala.gio.Application.LAYOUT.withName("parent_instance"));

    private static final MethodHandle GTK_APPLICATION_GET_ACTIVE_WINDOW =
            BindingSupport.lookup("gtk_application_get_active_window", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle GTK_APPLICATION_GET_WINDOWS =
            BindingSupport.lookup("gtk_application_get_windows", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle GTK_APPLICATION_NEW =
            BindingSupport.lookup("gtk_application_new", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));

    public static final ClassType<Class, Application> TYPE =
            ClassType.ofTypeGetter("gtk_application_get_type", Class::new, Application::new);

    protected Application(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Application of(String applicationId, Set<ApplicationFlags> flags) {
        MemoryAddress memoryAddress = BindingSupport.callThrowing(local -> (MemoryAddress)
                GTK_APPLICATION_NEW.invoke(local.allocateUtf8String(applicationId), BitField.toInt(flags)));
        return new Application(memoryAddress);
    }

    public Optional<Window> getActiveWindow() {
        MemoryAddress addr =
                BindingSupport.callThrowing(() -> (MemoryAddress) GTK_APPLICATION_GET_ACTIVE_WINDOW.invoke(address()));
        return MemoryAddress.NULL.equals(addr) ? Optional.empty() : Optional.of(Window.TYPE.wrap(addr));
    }

    public List getWindows() {
        MemoryAddress memoryAddress =
                BindingSupport.callThrowing(() -> (MemoryAddress) GTK_APPLICATION_GET_WINDOWS.invoke(address()));
        return List.view(memoryAddress, MemorySession.global());
    }

    public static class Class extends dev.ianjohnson.guatemala.gio.Application.Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                dev.ianjohnson.guatemala.gio.Application.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("window_added"),
                ADDRESS.withName("window_removed"),
                MemoryLayout.sequenceLayout(8, GPOINTER).withName("padding"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
