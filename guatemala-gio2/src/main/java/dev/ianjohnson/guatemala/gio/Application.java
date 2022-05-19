package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.Type;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.foreign.ValueLayout.*;

public class Application extends Object {
    private static final MethodHandle G_APPLICATION_GET_TYPE =
            BindingSupport.lookup("g_application_get_type", FunctionDescriptor.of(JAVA_LONG));
    private static final MethodHandle G_APPLICATION_RUN =
            BindingSupport.lookup("g_application_run", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS));

    private static final Type TYPE = Type.ofMethodHandle(G_APPLICATION_GET_TYPE);

    protected Application(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static Type getType() {
        return TYPE;
    }

    public int run(String[] args) {
        return BindingSupport.callThrowing(local -> {
            MemorySegment argv = local.allocateArray(ADDRESS, args.length + 1);
            for (int i = 0; i < args.length; i++) {
                argv.setAtIndex(ADDRESS, i, local.allocateUtf8String(args[i]));
            }
            argv.setAtIndex(ADDRESS, args.length, MemoryAddress.NULL);
            return (int) G_APPLICATION_RUN.invoke(getMemoryAddress(), args.length, argv);
        });
    }

    public void connectActivate(ActivateHandler handler) {
        MethodHandle handlerHandle = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                .findVirtual(ActivateHandler.class, "activate", MethodType.methodType(void.class)));
        connectSignal("activate", handlerHandle.bindTo(handler), FunctionDescriptor.ofVoid());
    }

    public interface ActivateHandler {
        void activate();
    }

    public static class Class extends Object.Class {
        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
