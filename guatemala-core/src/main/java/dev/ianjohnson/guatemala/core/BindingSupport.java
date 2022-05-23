package dev.ianjohnson.guatemala.core;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;

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

    public static GroupLayout structLayout(MemoryLayout... elements) {
        List<MemoryLayout> elementsWithPadding = new ArrayList<>(elements.length);
        long currentOffset = 0;
        for (MemoryLayout e : elements) {
            long paddingBits = currentOffset % e.bitAlignment();
            if (paddingBits > 0) {
                paddingBits = e.bitAlignment() - paddingBits;
                elementsWithPadding.add(MemoryLayout.paddingLayout(paddingBits));
                currentOffset += paddingBits;
            }
            assert currentOffset % e.bitAlignment() == 0;
            elementsWithPadding.add(e);
            currentOffset += e.bitSize();
        }
        return MemoryLayout.structLayout(elementsWithPadding.toArray(new MemoryLayout[0]));
    }

    public static <T> List<T> toList(
            MemoryAddress memoryAddress,
            int n,
            ValueLayout.OfAddress elemLayout,
            Function<MemoryAddress, ? extends T> converter) {
        List<T> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.set(i, converter.apply(memoryAddress.getAtIndex(elemLayout, i)));
        }
        return list;
    }

    public static <T> List<T> toList(
            MemorySegment memorySegment,
            ValueLayout.OfAddress elemLayout,
            Function<MemoryAddress, ? extends T> converter) {
        if (memorySegment.byteSize() % elemLayout.byteSize() != 0) {
            throw new IllegalArgumentException("memorySegment size is not a multiple of elemLayout size");
        }

        long longN = memorySegment.byteSize() / elemLayout.byteSize();
        if (longN > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Too many elements to fit in a List");
        }
        int n = (int) longN;

        return toList(memorySegment.address(), n, elemLayout, converter);
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
