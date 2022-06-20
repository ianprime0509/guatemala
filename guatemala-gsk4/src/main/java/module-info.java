module dev.ianjohnson.guatemala.gsk {
    requires static dev.ianjohnson.guatemala.annotation;
    requires static org.jetbrains.annotations;
    requires transitive dev.ianjohnson.guatemala.gdk;
    requires transitive dev.ianjohnson.guatemala.graphene;

    exports dev.ianjohnson.guatemala.gsk;
}
