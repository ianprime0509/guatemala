package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class Button extends Widget {
    private static final MethodHandle GTK_BUTTON_NEW_WITH_LABEL =
            BindingSupport.lookup("gtk_button_new_with_label", FunctionDescriptor.of(ADDRESS, ADDRESS));

    public static final ClassType<Class, Button> TYPE =
            ClassType.ofTypeGetter("gtk_button_get_type", Class::new, Button::new);

    protected Button(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Button ofLabel(String label) {
        return TYPE.wrapOwning(BindingSupport.callThrowing(
                local -> (MemoryAddress) GTK_BUTTON_NEW_WITH_LABEL.invoke(local.allocateUtf8String(label))));
    }

    public void connectClicked(ClickedHandler handler) {
        MethodHandle handlerHandle = BindingSupport.callThrowing(() ->
                MethodHandles.lookup().findVirtual(ClickedHandler.class, "clicked", MethodType.methodType(void.class)));
        connectSignal("clicked", handlerHandle.bindTo(handler), FunctionDescriptor.ofVoid());
    }

    public interface ClickedHandler {
        void clicked();
    }

    public static class Class extends Widget.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
