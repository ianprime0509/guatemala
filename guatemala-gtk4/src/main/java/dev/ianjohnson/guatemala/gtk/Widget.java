package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.InitiallyUnowned;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Widget extends InitiallyUnowned {
    private static final MethodHandle GTK_WIDGET_SET_HALIGN =
            BindingSupport.lookup("gtk_widget_set_halign", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT));
    private static final MethodHandle GTK_WIDGET_SET_VALIGN =
            BindingSupport.lookup("gtk_widget_set_valign", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT));
    private static final MethodHandle GTK_WIDGET_SHOW =
            BindingSupport.lookup("gtk_widget_show", FunctionDescriptor.ofVoid(ADDRESS));

    public static final ObjectType<Class, Widget> TYPE =
            ObjectType.ofTypeGetter("gtk_widget_get_type", Class::new, Widget::new);

    protected Widget(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public void setHalign(Align align) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_HALIGN.invoke(getMemoryAddress(), align.getValue()));
    }

    public void setValign(Align align) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_VALIGN.invoke(getMemoryAddress(), align.getValue()));
    }

    public void show() {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SHOW.invoke(getMemoryAddress()));
    }

    public static class Class extends InitiallyUnowned.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
