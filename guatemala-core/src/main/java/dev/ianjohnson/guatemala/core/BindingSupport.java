package dev.ianjohnson.guatemala.core;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.util.List;
import java.util.ServiceLoader;

public final class BindingSupport {
    private static final List<String> LIBS = findAndLoadLibraries();
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    private static final Cleaner CLEANER = Cleaner.create();

    private BindingSupport() {}

    public static MemorySegment lookup(String symbol) {
        return LOOKUP.lookup(symbol)
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find symbol '" + symbol + "' in any loaded library: " + LIBS));
    }

    public static MethodHandle lookup(String symbol, FunctionDescriptor descriptor) {
        return LINKER.downcallHandle(lookup(symbol), descriptor);
    }

    public static MemorySegment upcallStub(
            MethodHandle target, FunctionDescriptor functionDescriptor, MemorySession memorySession) {
        // TODO: catch exceptions?
        return LINKER.upcallStub(target, functionDescriptor, memorySession);
    }

    public static void registerCleanup(Object obj, Runnable action) {
        CLEANER.register(obj, action);
    }

    public static void runThrowing(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static void runThrowing(ThrowingRunnableWithLocal runnable) {
        try (MemorySession local = MemorySession.openConfined()) {
            runnable.run(local);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static <T> T callThrowing(ThrowingCallable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static <T> T callThrowing(ThrowingCallableWithLocal<T> callable) {
        try (MemorySession local = MemorySession.openConfined()) {
            return callable.call(local);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <X extends Throwable> X sneakyThrow(Throwable exception) throws X {
        throw (X) exception;
    }

    private static List<String> findAndLoadLibraries() {
        return ServiceLoader.load(NativeLibraryProvider.class).stream()
                .flatMap(provider -> provider.get().getLibraryNames().stream())
                .peek(System::loadLibrary)
                .toList();
    }

    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public interface ThrowingRunnableWithLocal {
        void run(MemorySession local) throws Throwable;
    }

    public interface ThrowingCallable<T> {
        T call() throws Throwable;
    }

    public interface ThrowingCallableWithLocal<T> {
        T call(MemorySession local) throws Throwable;
    }
}
