module dev.ianjohnson.guatemala.glib {
    requires static dev.ianjohnson.guatemala.annotation;
    requires static org.jetbrains.annotations;
    requires transitive dev.ianjohnson.guatemala.core;

    exports dev.ianjohnson.guatemala.glib;

    provides dev.ianjohnson.guatemala.core.NativeLibraryProvider with
            dev.ianjohnson.guatemala.glib.GLibNativeLibraryProvider;
}
