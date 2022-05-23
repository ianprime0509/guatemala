package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Set;

import static java.lang.foreign.ValueLayout.*;

// TODO: check types when viewing, etc.
public final class ObjectType<C extends Object.Class, T extends Object> extends Type {
    private static final MethodHandle G_OBJECT_REF =
            BindingSupport.lookup("g_object_ref", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle G_OBJECT_UNREF =
            BindingSupport.lookup("g_object_unref", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle G_TYPE_REGISTER_STATIC_SIMPLE = BindingSupport.lookup(
            "g_type_register_static_simple",
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT));

    private final Viewer<C> classViewer;
    private final Viewer<T> instanceViewer;
    private final Wrapper<T> instanceWrapper;
    private final OwningWrapper<T> instanceOwningWrapper;

    private ObjectType(
            long raw,
            Viewer<C> classViewer,
            Viewer<T> instanceViewer,
            Wrapper<T> instanceWrapper,
            OwningWrapper<T> instanceOwningWrapper) {
        super(raw);
        this.classViewer = Objects.requireNonNull(classViewer, "classViewer");
        this.instanceViewer = Objects.requireNonNull(instanceViewer, "instanceViewer");
        this.instanceWrapper = Objects.requireNonNull(instanceWrapper, "instanceWrapper");
        this.instanceOwningWrapper = Objects.requireNonNull(instanceOwningWrapper, "instanceOwningWrapper");
    }

    public static <C extends Object.Class, T extends Object> ObjectType<C, T> ofRaw(
            long raw, Viewer<C> classViewer, Viewer<T> instanceViewer) {
        return new ObjectType<>(
                raw,
                classViewer,
                instanceViewer,
                instanceWrapper(instanceViewer),
                instanceOwningWrapper(instanceViewer));
    }

    public static <C extends Object.Class, T extends Object> ObjectType<C, T> ofTypeGetter(
            String typeGetterName, Viewer<C> classViewer, Viewer<T> instanceViewer) {
        MethodHandle typeGetter = BindingSupport.lookup(typeGetterName, FunctionDescriptor.of(JAVA_LONG));
        return ofRaw(BindingSupport.callThrowing(() -> (long) typeGetter.invoke()), classViewer, instanceViewer);
    }

    public static <C extends Object.Class, T extends Object> ObjectType<C, T> register(
            ObjectType<? super C, ? super T> parentType,
            String typeName,
            MemoryLayout classLayout,
            Viewer<C> classViewer,
            ClassInitFunc<C> classInitFunc,
            MemoryLayout instanceLayout,
            Viewer<T> instanceViewer,
            InstanceInitFunc<T> instanceInitFunc,
            Set<TypeFlag> typeFlags) {
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
                Flag.toInt(typeFlags)));
        return ofRaw(raw, classViewer, instanceViewer);
    }

    private static <T extends Object> Wrapper<T> instanceWrapper(Viewer<T> instanceViewer) {
        return (addr) -> {
            BindingSupport.runThrowing(() -> G_OBJECT_REF.invoke(addr));
            T obj = instanceViewer.view(addr);
            BindingSupport.registerCleanup(obj, new UnrefAction(addr));
            return obj;
        };
    }

    private static <T extends Object> OwningWrapper<T> instanceOwningWrapper(Viewer<T> instanceViewer) {
        return (addr) -> {
            T obj = instanceViewer.view(addr);
            BindingSupport.registerCleanup(obj, new UnrefAction(addr));
            return obj;
        };
    }

    public C viewClass(MemoryAddress memoryAddress) {
        return classViewer.view(memoryAddress);
    }

    public T viewInstance(MemoryAddress memoryAddress) {
        return instanceViewer.view(memoryAddress);
    }

    public T wrapInstance(MemoryAddress memoryAddress) {
        return instanceWrapper.wrap(memoryAddress);
    }

    public T wrapInstanceOwning(MemoryAddress memoryAddress) {
        return instanceOwningWrapper.wrapOwning(memoryAddress);
    }

    public interface ClassInitFunc<C extends Object.Class> {
        void initClass(C clazz);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initClass", MethodType.methodType(void.class, MemoryAddress.class)));

            static <C extends Object.Class> Raw of(Viewer<C> classViewer, ClassInitFunc<C> classInitFunc) {
                return clazz -> classInitFunc.initClass(classViewer.view(clazz));
            }

            void initClass(MemoryAddress clazz);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }

    public interface InstanceInitFunc<T extends Object> {
        void initInstance(T instance);

        interface Raw {
            MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                    .findVirtual(Raw.class, "initInstance", MethodType.methodType(void.class, MemoryAddress.class)));

            static <T extends Object> Raw of(Viewer<T> instanceViewer, InstanceInitFunc<T> instanceInitFunc) {
                return instance -> instanceInitFunc.initInstance(instanceViewer.view(instance));
            }

            void initInstance(MemoryAddress instance);

            default MemorySegment toUpcallStub() {
                return BindingSupport.upcallStub(
                        HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
            }
        }
    }

    private record UnrefAction(MemoryAddress memoryAddress) implements Runnable {
        @Override
        public void run() {
            BindingSupport.runThrowing(() -> G_OBJECT_UNREF.invoke(memoryAddress));
        }
    }
}
