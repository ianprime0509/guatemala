module dev.ianjohnson.guatemala.gtk {
    requires static dev.ianjohnson.guatemala.annotation;
    requires transitive dev.ianjohnson.guatemala.gio;

    exports dev.ianjohnson.guatemala.gtk;

    provides dev.ianjohnson.guatemala.core.NativeLibraryProvider with
            dev.ianjohnson.guatemala.gtk.GtkNativeLibraryProvider;
}
