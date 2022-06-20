package dev.ianjohnson.guatemala.examples.gtk.complete;

import dev.ianjohnson.guatemala.examples.gtk.Example;

@Example(
        name = "Complete",
        description = "A simple, yet complete, application",
        source = "https://docs.gtk.org/gtk4/getting_started.html#a-trivial-application")
public final class Main {
    public static void main(String[] args) {
        ExampleApp.of().run(args);
    }
}
