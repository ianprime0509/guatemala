package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.glib.Error;
import dev.ianjohnson.guatemala.glib.GLibException;
import dev.ianjohnson.guatemala.gobject.Object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.*;

public class Builder extends Object {
    private static final MethodHandle GTK_BUILDER_ADD_FROM_STRING = BindingSupport.lookup(
            "gtk_builder_add_from_string", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_LONG, ADDRESS));
    private static final MethodHandle GTK_BUILDER_GET_OBJECT =
            BindingSupport.lookup("gtk_builder_get_object", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    private static final MethodHandle GTK_BUILDER_NEW =
            BindingSupport.lookup("gtk_builder_new", FunctionDescriptor.of(ADDRESS));

    protected Builder(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Builder of() {
        return newWithOwnership(Builder::new, () -> (MemoryAddress) GTK_BUILDER_NEW.invoke());
    }

    public static Builder ofMemoryAddress(MemoryAddress memoryAddress) {
        return ofMemoryAddress(memoryAddress, Builder::new);
    }

    public void addFromClasspath(String resource) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IllegalArgumentException("Classpath resource not found: " + resource);
        }
        try (is) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            is.transferTo(bytes);
            addFromString(bytes.toString(StandardCharsets.UTF_8));
        }
    }

    public void addFromString(String ui) {
        BindingSupport.runThrowing(local -> {
            MemorySegment errorOut = local.allocate(ADDRESS);
            boolean success = (boolean)
                    GTK_BUILDER_ADD_FROM_STRING.invoke(getMemoryAddress(), local.allocateUtf8String(ui), -1, errorOut);
            if (!success) {
                Error error = Error.wrapOwning(errorOut.get(ADDRESS, 0), MemorySession.global());
                throw new GLibException(error);
            }
        });
    }

    public <T extends Object> T getObject(String name, Function<MemoryAddress, T> constructor) {
        MemoryAddress memoryAddress = BindingSupport.callThrowing(local ->
                (MemoryAddress) GTK_BUILDER_GET_OBJECT.invoke(getMemoryAddress(), local.allocateUtf8String(name)));
        if (MemoryAddress.NULL.equals(memoryAddress)) {
            throw new IllegalArgumentException("No object named '" + name + "' defined in builder");
        }
        return constructor.apply(memoryAddress);
    }

    public static class Class extends Object.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
