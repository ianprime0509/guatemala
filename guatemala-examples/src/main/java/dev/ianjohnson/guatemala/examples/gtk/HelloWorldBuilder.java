package dev.ianjohnson.guatemala.examples.gtk;

import dev.ianjohnson.guatemala.gio.ApplicationFlag;
import dev.ianjohnson.guatemala.gtk.Application;
import dev.ianjohnson.guatemala.gtk.Builder;
import dev.ianjohnson.guatemala.gtk.Button;
import dev.ianjohnson.guatemala.gtk.Window;

import java.io.IOException;
import java.util.EnumSet;

// https://docs.gtk.org/gtk4/getting_started.html#packing-buttons-with-gtkbuilder
public class HelloWorldBuilder {
    public static void main(String[] args) {
        Application app = Application.of("org.gtk.example", EnumSet.noneOf(ApplicationFlag.class));
        app.connectActivate(() -> activate(app));
        System.exit(app.run(args));
    }

    private static void activate(Application app) {
        Builder builder = Builder.of();
        try {
            builder.addFromClasspath("dev/ianjohnson/guatemala/examples/gtk/builder.ui");
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        Window window = builder.getObject("window", Window.TYPE);
        window.setApplication(app);

        Button button1 = builder.getObject("button1", Button.TYPE);
        button1.connectClicked(HelloWorldBuilder::printHello);

        Button button2 = builder.getObject("button2", Button.TYPE);
        button2.connectClicked(HelloWorldBuilder::printHello);

        Button quitButton = builder.getObject("quit", Button.TYPE);
        quitButton.connectClicked(window::close);

        window.show();
    }

    private static void printHello() {
        System.out.println("Hello, world");
    }
}
