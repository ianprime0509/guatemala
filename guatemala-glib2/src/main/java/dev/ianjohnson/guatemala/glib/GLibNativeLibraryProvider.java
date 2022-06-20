package dev.ianjohnson.guatemala.glib;

import dev.ianjohnson.guatemala.core.NativeLibraryProvider;

import java.util.List;

public final class GLibNativeLibraryProvider implements NativeLibraryProvider {
    @Override
    public List<java.lang.String> getLibraryNames() {
        return List.of("glib-2.0");
    }
}
