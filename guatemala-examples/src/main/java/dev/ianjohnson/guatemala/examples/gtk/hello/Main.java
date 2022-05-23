package dev.ianjohnson.guatemala.examples.gtk.hello;

import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.gtk.*;

import java.util.EnumSet;

// https://docs.gtk.org/gtk4/getting_started.html#hello-world-in-c
class Main {
    public static void main(String[] args) {
        Application app = Application.of("org.gtk.example", EnumSet.noneOf(ApplicationFlag.class));
        app.connectActivate(() -> activate(app));
        System.exit(app.run(args));
    }

    private static void activate(Application app) {
        ApplicationWindow window = ApplicationWindow.of(app);
        window.setTitle("Window");
        window.setDefaultSize(200, 200);

        Box box = Box.of(Orientation.VERTICAL, 0);
        box.setHalign(Align.CENTER);
        box.setValign(Align.CENTER);

        window.setChild(box);

        Button button = Button.ofLabel("Hello World");

        button.connectClicked(() -> System.out.println("Hello World"));
        button.connectClicked(window::destroy);

        box.append(button);

        window.show();
    }
}
