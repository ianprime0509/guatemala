package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.core.OwningWrapper;
import dev.ianjohnson.guatemala.core.Viewer;
import dev.ianjohnson.guatemala.core.Wrapper;
import dev.ianjohnson.guatemala.glib.Addressable;
import dev.ianjohnson.guatemala.glib.ReferenceCounted;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class ReferenceCountedType<T extends Addressable & ReferenceCounted> extends Type
        implements Viewer<T>, Wrapper<T>, OwningWrapper<T> {
    private final Viewer<T> viewer;
    private final Wrapper<T> wrapper;
    private final OwningWrapper<T> owningWrapper;

    protected ReferenceCountedType(long raw, Viewer<T> viewer) {
        this(raw, viewer, wrapper(viewer), owningWrapper(viewer));
    }

    protected ReferenceCountedType(long raw, Viewer<T> viewer, Wrapper<T> wrapper, OwningWrapper<T> owningWrapper) {
        super(raw);
        this.viewer = Objects.requireNonNull(viewer, "viewer");
        this.wrapper = Objects.requireNonNull(wrapper, "wrapper");
        this.owningWrapper = Objects.requireNonNull(owningWrapper, "owningWrapper");
    }

    public static <T extends Addressable & ReferenceCounted> ReferenceCountedType<T> ofRaw(long raw, Viewer<T> viewer) {
        return new ReferenceCountedType<>(raw, viewer);
    }

    public static <T extends Addressable & ReferenceCounted> ReferenceCountedType<T> ofTypeGetter(
            java.lang.String typeGetterName, Viewer<T> viewer) {
        MethodHandle typeGetter = BindingSupport.lookup(typeGetterName, FunctionDescriptor.of(JAVA_LONG));
        return ofRaw(BindingSupport.callThrowing(() -> (long) typeGetter.invoke()), viewer);
    }

    private static <T extends Addressable & ReferenceCounted> Wrapper<T> wrapper(Viewer<T> viewer) {
        return addr -> {
            T wrapped = viewer.view(addr);
            wrapped.ref();
            BindingSupport.registerCleanup(wrapped, new UnrefAction<>(wrapped.address(), viewer));
            return wrapped;
        };
    }

    private static <T extends Addressable & ReferenceCounted> OwningWrapper<T> owningWrapper(Viewer<T> viewer) {
        return addr -> {
            T wrapped = viewer.view(addr);
            BindingSupport.registerCleanup(wrapped, new UnrefAction<>(wrapped.address(), viewer));
            return wrapped;
        };
    }

    @Override
    public final T view(MemoryAddress address) {
        return viewer.view(address);
    }

    @Override
    public final T wrap(MemoryAddress address) {
        return wrapper.wrap(address);
    }

    @Override
    public final T wrapOwning(MemoryAddress address) {
        return owningWrapper.wrapOwning(address);
    }

    private record UnrefAction<T extends ReferenceCounted>(MemoryAddress address, Viewer<T> viewer)
            implements Runnable {
        @Override
        public void run() {
            viewer.view(address).unref();
        }
    }
}
