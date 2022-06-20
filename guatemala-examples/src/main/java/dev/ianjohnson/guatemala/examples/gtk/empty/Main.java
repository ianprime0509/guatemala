package dev.ianjohnson.guatemala.examples.gtk.empty;

import dev.ianjohnson.guatemala.gio.ApplicationFlags;
import dev.ianjohnson.guatemala.gtk.Application;
import dev.ianjohnson.guatemala.gtk.ApplicationWindow;

import java.util.EnumSet;

// https://docs.gtk.org/gtk4/getting_started.html#basics
class Main {
    public static void main(String[] args) {
        Application app = Application.of("org.gtk.example", EnumSet.noneOf(ApplicationFlags.class));
        app.connectActivate(() -> activate(app));
        System.exit(app.run(args));
    }

    private static void activate(Application app) {
        ApplicationWindow window = ApplicationWindow.of(app);
        window.setTitle("Window");
        window.setDefaultSize(200, 200);
        window.show();
    }
}
