package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Window extends Widget {
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

    protected Window(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Window ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, Window::new);
    }

    public void close() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_CLOSE.invoke(getMemoryAddress()));
    }

    public void destroy() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_DESTROY.invoke(getMemoryAddress()));
    }

    public void present() {
        BindingSupport.runThrowing(() -> GTK_WINDOW_PRESENT.invoke(getMemoryAddress()));
    }

    public void setApplication(Application application) {
        BindingSupport.runThrowing(
                () -> GTK_WINDOW_SET_APPLICATION.invoke(getMemoryAddress(), application.getMemoryAddress()));
    }

    public void setChild(Widget child) {
        BindingSupport.runThrowing(() -> GTK_WINDOW_SET_CHILD.invoke(getMemoryAddress(), child.getMemoryAddress()));
    }

    public void setDefaultSize(int width, int height) {
        BindingSupport.runThrowing(() -> GTK_WINDOW_SET_DEFAULT_SIZE.invoke(getMemoryAddress(), width, height));
    }

    public void setTitle(String title) {
        BindingSupport.runThrowing(
                local -> GTK_WINDOW_SET_TITLE.invoke(getMemoryAddress(), local.allocateUtf8String(title)));
    }

    public static class Class extends Widget.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
