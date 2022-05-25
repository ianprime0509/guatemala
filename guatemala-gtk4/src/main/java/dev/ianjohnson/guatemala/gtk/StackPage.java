package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.MemoryAddress;

public final class StackPage extends Object {
    public static final ObjectType<Class, StackPage> TYPE =
            ObjectType.ofTypeGetter("gtk_stack_page_get_type", Class::new, StackPage::new);

    private StackPage(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static final class Class extends Object.Class {
        private Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
