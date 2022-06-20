package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.glib.GLib;
import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class File extends Object {
    private static final MethodHandle G_FILE_GET_URI =
            BindingSupport.lookup("g_file_get_uri", FunctionDescriptor.of(ADDRESS, ADDRESS));
    private static final MethodHandle G_FILE_NEW_FOR_URI =
            BindingSupport.lookup("g_file_new_for_uri", FunctionDescriptor.of(ADDRESS, ADDRESS));

    public static final ClassType<Class, File> TYPE =
            ClassType.ofTypeGetter("g_file_get_type", Class::new, File::new);

    protected File(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static File ofPath(Path path) {
        return ofUri(path.toUri());
    }

    public static File ofUri(String uri) {
        return TYPE.wrapOwning(BindingSupport.callThrowing(
                local -> (MemoryAddress) G_FILE_NEW_FOR_URI.invoke(local.allocateUtf8String(uri))));
    }

    public static File ofUri(URI uri) {
        return ofUri(uri.toString());
    }

    public URI getUri() {
        try {
            return new URI(getUriAsString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid file URI", e);
        }
    }

    public String getUriAsString() {
        MemoryAddress uriAddr = BindingSupport.callThrowing(() -> (MemoryAddress) G_FILE_GET_URI.invoke(address()));
        try {
            return uriAddr.getUtf8String(0);
        } finally {
            GLib.free(uriAddr);
        }
    }

    public Path toPath() {
        return Path.of(getUri());
    }

    public static class Class extends Object.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
