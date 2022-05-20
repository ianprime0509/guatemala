package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.glib.Types;
import dev.ianjohnson.guatemala.gobject.Type;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.MethodHandle;
import java.nio.channels.FileChannel;
import java.util.Set;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.ValueLayout.*;

public class Application extends dev.ianjohnson.guatemala.gio.Application {
    private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(
            dev.ianjohnson.guatemala.gio.Application.getMemoryLayout().withName("parent_instance"));

    private static final MethodHandle GTK_APPLICATION_GET_TYPE =
            BindingSupport.lookup("gtk_application_get_type", FunctionDescriptor.of(JAVA_LONG));
    private static final MethodHandle GTK_APPLICATION_NEW =
            BindingSupport.lookup("gtk_application_new", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));

    private static final Type TYPE = Type.ofMethodHandle(GTK_APPLICATION_GET_TYPE);

    protected Application(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static MemoryLayout getMemoryLayout() {
        return MEMORY_LAYOUT;
    }

    public static Type getType() {
        return TYPE;
    }

    public static Application of(String applicationId, Set<ApplicationFlag> flags) {
        MemoryAddress memoryAddress = BindingSupport.callThrowing(local -> (MemoryAddress)
                GTK_APPLICATION_NEW.invoke(local.allocateUtf8String(applicationId), ApplicationFlag.toInt(flags)));
        return new Application(memoryAddress);
    }

    public static Application ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, Application::new);
    }

    public static class Class extends dev.ianjohnson.guatemala.gio.Application.Class {
        private static final MemoryLayout MEMORY_LAYOUT = MemoryLayout.structLayout(
                dev.ianjohnson.guatemala.gio.Application.Class.getMemoryLayout().withName("parent_class"),
                ADDRESS.withName("window_added"),
                ADDRESS.withName("window_removed"),
                MemoryLayout.sequenceLayout(8, GPOINTER).withName("padding"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public static MemoryLayout getMemoryLayout() {
            return MEMORY_LAYOUT;
        }
    }
}
