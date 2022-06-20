package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;

import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Window extends Widget {
    public static final MemoryLayout LAYOUT = BindingSupport.structLayout(Widget.LAYOUT.withName("parent_instance"));

    private static final MethodHandle GTK_WINDOW_CLOSE =
            BindingSupport.lookup("gtk_window_close", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle GTK_WINDOW_DESTROY =
            BindingSupport.lookup("gtk_window_destroy", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle GTK_WINDOW_PRESENT =
            BindingSupport.lookup("gtk_window_present", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle GTK_WINDOW_SET_APPLICATION =
            BindingSupport.lookup("gtk_window_set_application", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle GTK_WINDOW_SET_CHILD =
            BindingSupport.lookup("gtk_window_set_child", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    private static final MethodHandle GTK_WINDOW_SET_DEFAULT_SIZE = BindingSupport.lookup(
            "gtk_window_set_default_size", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, JAVA_INT));
    private static final MethodHandle GTK_WINDOW_SET_TITLE =
            BindingSupport.lookup("gtk_window_set_title", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));

    public static final ClassType<Class, Window> TYPE =
            ClassType.ofTypeGetter("gtk_window_get_type", Class::new, Window::new);

    protected Window(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public void close() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_CLOSE.invoke(address()));
    }

    public void destroy() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_DESTROY.invoke(address()));
    }

    public void present() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_PRESENT.invoke(address()));
    }

    public void setApplication(Application application) {
        BindingSupport.runThrowing(
                () -> GTK_WINDOW_SET_APPLICATION.invoke(address(), application.address()));
    }

    public void setChild(Widget child) {
        BindingSupport.runThrowing(() -> GTK_WINDOW_SET_CHILD.invoke(address(), child.address()));
    }

    public void setDefaultSize(int width, int height) {
        BindingSupport.runThrowing(() -> GTK_WINDOW_SET_DEFAULT_SIZE.invoke(address(), width, height));
    }

    public void setTitle(String title) {
        BindingSupport.runThrowing(
                local -> GTK_WINDOW_SET_TITLE.invoke(address(), local.allocateUtf8String(title)));
    }

    public static class Class extends Widget.Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                Widget.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("activate_focus"),
                ADDRESS.withName("activate_default"),
                ADDRESS.withName("keys_changed"),
                ADDRESS.withName("enable_debugging"),
                ADDRESS.withName("close_request"),
                MemoryLayout.sequenceLayout(8, GPOINTER).withName("padding"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
