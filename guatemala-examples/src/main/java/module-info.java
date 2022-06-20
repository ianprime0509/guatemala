module dev.ianjohnson.guatemala.examples {
    requires dev.ianjohnson.guatemala.gtk;
    requires io.github.classgraph;

    opens dev.ianjohnson.guatemala.examples.gtk.builder;
    opens dev.ianjohnson.guatemala.examples.gtk.complete;
    opens dev.ianjohnson.guatemala.examples.gtk.empty;
    opens dev.ianjohnson.guatemala.examples.gtk.grid;
    opens dev.ianjohnson.guatemala.examples.gtk.hello;
}
