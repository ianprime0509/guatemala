package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.core.BitField;
import dev.ianjohnson.guatemala.core.Viewer;
import dev.ianjohnson.guatemala.glib.Addressable;
import dev.ianjohnson.guatemala.glib.ReferenceCounted;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Set;

import static java.lang.foreign.ValueLayout.*;

// TODO: check types when viewing, etc.
public final class ClassType<C extends Addressable, T extends Addressable & ReferenceCounted>
        extends ReferenceCountedType<T> {
    private static final MethodHandle G_TYPE_REGISTER_STATIC_SIMPLE = BindingSupport.lookup(
            "g_type_register_static_simple",
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT));

    private final Viewer<C> classViewer;

    private ClassType(long raw, Viewer<C> classViewer, Viewer<T> instanceViewer) {
        super(raw, instanceViewer);
        this.classViewer = Objects.requireNonNull(classViewer, "classViewer");
    }

    public static <C extends Addressable, T extends Addressable & ReferenceCounted> ClassType<C, T> ofRaw(
            long raw, Viewer<C> classViewer, Viewer<T> instanceViewer) {
        return new ClassType<>(raw, classViewer, instanceViewer);
    }

    public static <C extends Addressable, T extends Addressable & ReferenceCounted> ClassType<C, T> ofTypeGetter(
            String typeGetterName, Viewer<C> classViewer, Viewer<T> instanceViewer) {
        MethodHandle typeGetter = BindingSupport.lookup(typeGetterName, FunctionDescriptor.of(JAVA_LONG));
        return ofRaw(BindingSupport.callThrowing(() -> (long) typeGetter.invoke()), classViewer, instanceViewer);
    }

    public static <C extends Addressable, T extends Addressable & ReferenceCounted> ClassType<C, T> register(
            ClassType<? super C, ? super T> parentType,
            String typeName,
            MemoryLayout classLayout,
            Viewer<C> classViewer,
            ClassInitFunc<C> classInitFunc,
            MemoryLayout instanceLayout,
            Viewer<T> instanceViewer,
            InstanceInitFunc<T> instanceInitFunc,
            Set<TypeFlags> typeFlags) {
        if (classLayout.byteSize() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Class size is too large");
        }
        if (instanceLayout.byteSize() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Instance size is too large");
        }
        long raw = BindingSupport.callThrowing(local -> (long) G_TYPE_REGISTER_STATIC_SIMPLE.invoke(
                parentType.getRaw(),
                local.allocateUtf8String(typeName),
                (int) classLayout.byteSize(),
                ClassInitFunc.Raw.of(classViewer, classInitFunc).toUpcallStub(),
                (int) instanceLayout.byteSize(),
                InstanceInitFunc.Raw.of(instanceViewer, instanceInitFunc).toUpcallStub(),
                BitField.toInt(typeFlags)));
        return ofRaw(raw, classViewer, instanceViewer);
    }

    public C viewClass(MemoryAddress memoryAddress) {
        return classViewer.view(memoryAddress);
    }

    public interface ClassInitFunc<C extends Addressable> {
        void initClass(C clazz);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initClass", MethodType.methodType(void.class, MemoryAddress.class)));

            static <C extends Addressable> Raw of(Viewer<C> classViewer, ClassInitFunc<C> classInitFunc) {
                return clazz -> classInitFunc.initClass(classViewer.view(clazz));
            }

            void initClass(MemoryAddress clazz);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }

    public interface InstanceInitFunc<T extends Addressable & ReferenceCounted> {
        void initInstance(T instance);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initInstance", MethodType.methodType(void.class, MemoryAddress.class)));

            static <T extends Addressable & ReferenceCounted> Raw of(
                    Viewer<T> instanceViewer, InstanceInitFunc<T> instanceInitFunc) {
                return instance -> instanceInitFunc.initInstance(instanceViewer.view(instance));
            }

            void initInstance(MemoryAddress instance);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }
}
