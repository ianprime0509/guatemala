module dev.ianjohnson.guatemala.gio {
    requires transitive dev.ianjohnson.guatemala.gobject;

    exports dev.ianjohnson.guatemala.gio;

    provides dev.ianjohnson.guatemala.core.NativeLibraryProvider with
            dev.ianjohnson.guatemala.gio.GioNativeLibraryProvider;
}
