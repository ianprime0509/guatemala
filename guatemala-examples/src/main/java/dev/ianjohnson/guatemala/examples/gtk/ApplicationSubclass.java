package dev.ianjohnson.guatemala.examples.gtk;

import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.gio.File;
import dev.ianjohnson.guatemala.gobject.ObjectType;
import dev.ianjohnson.guatemala.gobject.TypeFlag;
import dev.ianjohnson.guatemala.gobject.Value;
import dev.ianjohnson.guatemala.gtk.Application;
import dev.ianjohnson.guatemala.gtk.ApplicationWindow;

import java.lang.foreign.MemoryAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

// https://docs.gtk.org/gtk4/getting_started.html#a-trivial-application
public class ApplicationSubclass {
    public static void main(String[] args) {
        ExampleApp.of().run(args);
    }

    private static class ExampleApp extends Application {
        public static final ObjectType<Class, ExampleApp> TYPE = ObjectType.register(
                Application.TYPE,
                "ExampleApp",
                Class::new,
                Class::init,
                ExampleApp::new,
                ExampleApp::init,
                EnumSet.noneOf(TypeFlag.class));

        protected ExampleApp(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public static ExampleApp of() {
            return newWithProperties(
                    TYPE,
                    Map.of(
                            "application-id", Value.of("org.gtk.exampleapp"),
                            "flags", Value.of(ApplicationFlag.toInt(EnumSet.of(ApplicationFlag.HANDLES_OPEN)))));
        }

        private void init() {}

        private void activate() {
            ApplicationWindow window = ApplicationWindow.of(this);
            window.present();
        }

        private void open(List<File> files, String hint) {}

        private static class Class extends Application.Class {
            protected Class(MemoryAddress memoryAddress) {
                super(memoryAddress);
            }

            void init() {
                setActivate(TYPE, ExampleApp::activate);
                setOpen(TYPE, ExampleApp::open);
            }
        }
    }

    private static class ExampleAppWindow extends ApplicationWindow {
        public static final ObjectType<Class, ExampleAppWindow> TYPE = ObjectType.register(
                ApplicationWindow.TYPE,
                "ExampleAppWindow",
                Class::new,
                Class::init,
                ExampleAppWindow::new,
                ExampleAppWindow::init,
                EnumSet.noneOf(TypeFlag.class));

        protected ExampleAppWindow(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public static ExampleAppWindow of(ExampleApp app) {
            return newWithProperties(TYPE, Map.of("application", Value.of(app)));
        }

        private void init() {}

        private static class Class extends ApplicationWindow.Class {
            protected Class(MemoryAddress memoryAddress) {
                super(memoryAddress);
            }

            void init() {}
        }
    }
}
