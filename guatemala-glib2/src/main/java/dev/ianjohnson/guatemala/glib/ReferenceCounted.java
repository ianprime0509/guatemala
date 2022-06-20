package dev.ianjohnson.guatemala.glib;

public interface ReferenceCounted {
    void ref();

    void unref();
}
