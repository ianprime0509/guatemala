package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.core.NativeLibraryProvider;

import java.util.List;

public final class GioNativeLibraryProvider implements NativeLibraryProvider {
    @Override
    public List<String> getLibraryNames() {
        return List.of("gio-2.0");
    }
}
