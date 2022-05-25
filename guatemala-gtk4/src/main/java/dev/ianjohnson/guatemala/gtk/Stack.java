package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public final class Stack extends Widget {
    public static final ObjectType<Class, Stack> TYPE =
            ObjectType.ofTypeGetter("gtk_stack_get_type", Class::new, Stack::new);

    private static final MethodHandle GTK_STACK_ADD_TITLED = BindingSupport.lookup(
            "gtk_stack_add_titled", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS));

    private Stack(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public StackPage addTitled(Widget child, String name, String title) {
        return StackPage.TYPE.wrapInstance(
                BindingSupport.callThrowing(local -> (MemoryAddress) GTK_STACK_ADD_TITLED.invoke(
                        getMemoryAddress(),
                        child.getMemoryAddress(),
                        local.allocateUtf8String(name),
                        local.allocateUtf8String(title))));
    }

    public static final class Class extends Widget.Class {
        private Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
