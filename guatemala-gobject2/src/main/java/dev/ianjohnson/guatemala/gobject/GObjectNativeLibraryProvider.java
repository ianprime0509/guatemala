package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.NativeLibraryProvider;

import java.util.List;

public final class GObjectNativeLibraryProvider implements NativeLibraryProvider {
    @Override
    public List<String> getLibraryNames() {
        return List.of("gobject-2.0");
    }
}
