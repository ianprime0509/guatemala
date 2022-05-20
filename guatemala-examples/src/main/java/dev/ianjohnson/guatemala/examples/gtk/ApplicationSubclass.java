package dev.ianjohnson.guatemala.examples.gtk;

import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.gio.File;
import dev.ianjohnson.guatemala.gobject.Type;
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
        private static final Type TYPE = Type.registerStatic(
                Application.getType(),
                "ExampleApp",
                Class::new,
                Class::init,
                ExampleApp::ofMemoryAddress,
                ExampleApp::init,
                EnumSet.noneOf(TypeFlag.class));

        protected ExampleApp(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public static ExampleApp of() {
            return newWithProperties(
                    TYPE,
                    ExampleApp::new,
                    Map.of(
                            "application-id", Value.of("org.gtk.exampleapp"),
                            "flags", Value.of(ApplicationFlag.toInt(EnumSet.of(ApplicationFlag.HANDLES_OPEN)))));
        }

        public static ExampleApp ofMemoryAddress(MemoryAddress memoryAddress) {
            return ofMemoryAddress(memoryAddress, ExampleApp::new);
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
                setActivate(ExampleApp::ofMemoryAddress, ExampleApp::activate);
                setOpen(ExampleApp::ofMemoryAddress, ExampleApp::open);
            }
        }
    }
}
