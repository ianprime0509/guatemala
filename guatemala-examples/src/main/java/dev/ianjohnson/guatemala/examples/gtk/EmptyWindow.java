package dev.ianjohnson.guatemala.examples.gtk;

import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.gtk.Application;
import dev.ianjohnson.guatemala.gtk.ApplicationWindow;

import java.util.EnumSet;

// https://docs.gtk.org/gtk4/getting_started.html#basics
class EmptyWindow {
    public static void main(String[] args) {
        Application app = Application.of("org.gtk.example", EnumSet.noneOf(ApplicationFlag.class));
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
