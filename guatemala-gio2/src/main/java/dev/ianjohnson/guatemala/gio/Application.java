package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.ObjectType;
import dev.ianjohnson.guatemala.gobject.Type;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;

import static dev.ianjohnson.guatemala.glib.Types.GINT;
import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Application extends Object {
    public static final MemoryLayout LAYOUT =
            MemoryLayout.structLayout(Object.LAYOUT.withName("parent_instance"), ADDRESS.withName("priv"));

    private static final MethodHandle G_APPLICATION_RUN =
            BindingSupport.lookup("g_application_run", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS));

    public static final ObjectType<Class, Application> TYPE =
            ObjectType.ofTypeGetter("g_application_get_type", Class::new, Application::new);

    protected Application(MemoryAddress memoryAddress) {
        super(memoryAddress);
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
        public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
                Object.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("startup"),
                ADDRESS.withName("activate"),
                ADDRESS.withName("open"),
                ADDRESS.withName("command_line"),
                ADDRESS.withName("local_command_line"),
                ADDRESS.withName("before_emit"),
                ADDRESS.withName("after_emit"),
                ADDRESS.withName("add_platform_data"),
                ADDRESS.withName("quit_mainloop"),
                ADDRESS.withName("run_mainloop"),
                ADDRESS.withName("shutdown"),
                ADDRESS.withName("dbus_register"),
                ADDRESS.withName("handle_local_options"),
                ADDRESS.withName("name_lost"),
                MemoryLayout.sequenceLayout(7, GPOINTER).withName("padding"));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public <T extends Application> void setStartup(ObjectType<?, T> type, StartupFunc<T> startup) {
            MemorySegment upcallStub = StartupFunc.Raw.of(type, startup).toUpcallStub();
            getMemorySegment().set(ADDRESS, LAYOUT.byteOffset(groupElement("startup")), upcallStub);
        }

        public <T extends Application> void setActivate(ObjectType<?, T> type, ActivateFunc<T> activate) {
            MemorySegment upcallStub = ActivateFunc.Raw.of(type, activate).toUpcallStub();
            getMemorySegment().set(ADDRESS, LAYOUT.byteOffset(groupElement("activate")), upcallStub);
        }

        public <T extends Application> void setOpen(ObjectType<?, T> type, OpenFunc<T> open) {
            MemorySegment upcallStub = OpenFunc.Raw.of(type, open).toUpcallStub();
            getMemorySegment().set(ADDRESS, LAYOUT.byteOffset(groupElement("open")), upcallStub);
        }

        private MemorySegment getMemorySegment() {
            return MemorySegment.ofAddress(getMemoryAddress(), LAYOUT.byteSize(), MemorySession.global());
        }

        public interface StartupFunc<T extends Application> {
            void startup(T application);

            interface Raw {
                MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                        .findVirtual(Raw.class, "startup", MethodType.methodType(void.class, MemoryAddress.class)));

                static <T extends Application> Raw of(ObjectType<?, T> type, StartupFunc<T> startupFunc) {
                    return (application) -> startupFunc.startup(type.wrapInstance(application));
                }

                void startup(MemoryAddress application);

                default MemorySegment toUpcallStub() {
                    return BindingSupport.upcallStub(
                            HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
                }
            }
        }

        public interface ActivateFunc<T extends Application> {
            void activate(T application);

            interface Raw {
                MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                        .findVirtual(Raw.class, "activate", MethodType.methodType(void.class, MemoryAddress.class)));

                static <T extends Application> Raw of(ObjectType<?, T> type, ActivateFunc<T> activateFunc) {
                    return (application) -> activateFunc.activate(type.wrapInstance(application));
                }

                void activate(MemoryAddress application);

                default MemorySegment toUpcallStub() {
                    return BindingSupport.upcallStub(
                            HANDLE.bindTo(this), FunctionDescriptor.ofVoid(ADDRESS), MemorySession.global());
                }
            }
        }

        public interface OpenFunc<T extends Application> {
            void open(T application, List<File> files, String hint);

            interface Raw {
                MethodHandle HANDLE = BindingSupport.callThrowing(() -> MethodHandles.lookup()
                        .findVirtual(
                                Raw.class,
                                "open",
                                MethodType.methodType(
                                        void.class,
                                        MemoryAddress.class,
                                        MemoryAddress.class,
                                        int.class,
                                        MemoryAddress.class)));

                static <T extends Application> Raw of(ObjectType<?, T> type, OpenFunc<T> openFunc) {
                    return (application, files, nFiles, hint) -> openFunc.open(
                            type.wrapInstance(application),
                            BindingSupport.toList(files, nFiles, ADDRESS, File.TYPE::wrapInstance),
                            hint.getUtf8String(0));
                }

                void open(MemoryAddress application, MemoryAddress files, int nFiles, MemoryAddress hint);

                default MemorySegment toUpcallStub() {
                    return BindingSupport.upcallStub(
                            HANDLE.bindTo(this),
                            FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, GINT, ADDRESS),
                            MemorySession.global());
                }
            }
        }
    }
}
