package dev.ianjohnson.guatemala.examples.gtk.subclass;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;
import dev.ianjohnson.guatemala.gobject.TypeFlags;
import dev.ianjohnson.guatemala.gobject.Value;
import dev.ianjohnson.guatemala.gtk.ApplicationWindow;

import java.io.IOException;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.util.EnumSet;
import java.util.Map;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;

class ExampleAppWindow extends ApplicationWindow {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(ApplicationWindow.LAYOUT.withName("parent_instance"), ADDRESS.withName("stack"));
    public static final ClassType<Class, ExampleAppWindow> TYPE = ClassType.register(
            ApplicationWindow.TYPE,
            "ExampleAppWindow",
            Class.LAYOUT,
            Class::new,
            Class::init,
            LAYOUT,
            ExampleAppWindow::new,
            ExampleAppWindow::init,
            EnumSet.noneOf(TypeFlags.class));

    protected ExampleAppWindow(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static ExampleAppWindow of(ExampleApp app) {
        return newWithProperties(TYPE, Map.of("application", Value.of(app)));
    }

    private void init() {
        initTemplate();
    }

    private static class Class extends ApplicationWindow.Class {
        public static final MemoryLayout LAYOUT =
                BindingSupport.structLayout(ApplicationWindow.Class.LAYOUT.withName("parent_class"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        private void init() {
            try {
                setTemplateFromClasspathResource(getClass(), "window.ui");
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            bindTemplateChild("stack", false, ExampleAppWindow.LAYOUT.byteOffset(groupElement("stack")));
        }
    }
}
