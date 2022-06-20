package dev.ianjohnson.guatemala.examples.gtk.grid;

import dev.ianjohnson.guatemala.gio.ApplicationFlags;
import dev.ianjohnson.guatemala.gtk.Application;
import dev.ianjohnson.guatemala.gtk.ApplicationWindow;
import dev.ianjohnson.guatemala.gtk.Button;
import dev.ianjohnson.guatemala.gtk.Grid;

import java.util.EnumSet;

// https://docs.gtk.org/gtk4/getting_started.html#packing-buttons
class Main {
    public static void main(String[] args) {
        Application app = Application.of("org.gtk.example", EnumSet.noneOf(ApplicationFlags.class));
        app.connectActivate(() -> activate(app));
        System.exit(app.run(args));
    }

    private static void activate(Application app) {
        ApplicationWindow window = ApplicationWindow.of(app);
        window.setTitle("Window");

        Grid grid = Grid.of();

        window.setChild(grid);

        Button button1 = Button.ofLabel("Button 1");
        button1.connectClicked(Main::printHello);

        grid.attach(button1, 0, 0, 1, 1);

        Button button2 = Button.ofLabel("Button 2");
        button2.connectClicked(Main::printHello);

        grid.attach(button2, 1, 0, 1, 1);

        Button quitButton = Button.ofLabel("Quit");
        quitButton.connectClicked(window::destroy);

        grid.attach(quitButton, 0, 1, 2, 1);

        window.show();
    }

    private static void printHello() {
        System.out.println("Hello World");
    }
}
