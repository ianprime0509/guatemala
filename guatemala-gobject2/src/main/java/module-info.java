module dev.ianjohnson.guatemala.gobject {
    requires static dev.ianjohnson.guatemala.annotation;
    requires static org.jetbrains.annotations;
    requires transitive dev.ianjohnson.guatemala.glib;

    exports dev.ianjohnson.guatemala.gobject;

    provides dev.ianjohnson.guatemala.core.NativeLibraryProvider with dev.ianjohnson.guatemala.gobject.GObjectNativeLibraryProvider;
}
