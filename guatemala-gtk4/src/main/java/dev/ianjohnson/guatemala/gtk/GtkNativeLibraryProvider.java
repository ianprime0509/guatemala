package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.NativeLibraryProvider;

import java.util.List;

public final class GtkNativeLibraryProvider implements NativeLibraryProvider {
    @Override
    public List<String> getLibraryNames() {
        return List.of("gtk-4");
    }
}
