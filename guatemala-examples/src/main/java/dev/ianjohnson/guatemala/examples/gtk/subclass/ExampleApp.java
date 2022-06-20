package dev.ianjohnson.guatemala.examples.gtk.subclass;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gio.ApplicationFlags;
import dev.ianjohnson.guatemala.gio.File;
import dev.ianjohnson.guatemala.gobject.ClassType;
import dev.ianjohnson.guatemala.gobject.TypeFlags;
import dev.ianjohnson.guatemala.gobject.Value;
import dev.ianjohnson.guatemala.gtk.Application;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

class ExampleApp extends Application {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(Application.LAYOUT.withName("parent_instance"));
    public static final ClassType<Class, ExampleApp> TYPE = ClassType.register(
            Application.TYPE,
            "ExampleApp",
            Class.LAYOUT,
            Class::new,
            Class::init,
            LAYOUT,
            ExampleApp::new,
            ExampleApp::init,
            EnumSet.noneOf(TypeFlags.class));

    protected ExampleApp(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static ExampleApp of() {
        return newWithProperties(
                TYPE,
                Map.of(
                        "application-id", Value.of("org.gtk.exampleapp"),
                        "flags", Value.of(EnumSet.of(ApplicationFlags.HANDLES_OPEN))));
    }

    private void init() {}

    private void activate() {
        ExampleAppWindow.of(this).present();
    }

    private void open(List<File> files, String hint) {
        ExampleAppWindow win = getActiveWindow()
                .map(activeWindow -> activeWindow.cast(ExampleAppWindow.TYPE))
                .orElseGet(() -> ExampleAppWindow.of(this));
        win.present();
    }

    private static class Class extends Application.Class {
        public static final MemoryLayout LAYOUT =
                BindingSupport.structLayout(Application.Class.LAYOUT.withName("parent_class"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        private void init() {
            setActivate(TYPE, ExampleApp::activate);
            setOpen(TYPE, ExampleApp::open);
        }
    }
}
