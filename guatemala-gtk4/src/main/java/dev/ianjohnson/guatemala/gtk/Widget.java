package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.glib.Bytes;
import dev.ianjohnson.guatemala.gobject.InitiallyUnowned;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.io.IOException;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;

import static dev.ianjohnson.guatemala.glib.Types.GBOOLEAN;
import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.ValueLayout.*;

public class Widget extends InitiallyUnowned {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(InitiallyUnowned.LAYOUT.withName("parent_instance"), ADDRESS.withName("priv"));
    public static final ObjectType<Class, Widget> TYPE =
            ObjectType.ofTypeGetter("gtk_widget_get_type", Class::new, Widget::new);

    private static final MethodHandle GTK_WIDGET_INIT_TEMPLATE =
            BindingSupport.lookup("gtk_widget_init_template", FunctionDescriptor.ofVoid(ADDRESS));
    private static final MethodHandle GTK_WIDGET_SET_HALIGN =
            BindingSupport.lookup("gtk_widget_set_halign", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT));
    private static final MethodHandle GTK_WIDGET_SET_HEXPAND =
            BindingSupport.lookup("gtk_widget_set_hexpand", FunctionDescriptor.ofVoid(ADDRESS, GBOOLEAN));
    private static final MethodHandle GTK_WIDGET_SET_VALIGN =
            BindingSupport.lookup("gtk_widget_set_valign", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT));
    private static final MethodHandle GTK_WIDGET_SET_VEXPAND =
            BindingSupport.lookup("gtk_widget_set_vexpand", FunctionDescriptor.ofVoid(ADDRESS, GBOOLEAN));
    private static final MethodHandle GTK_WIDGET_SHOW =
            BindingSupport.lookup("gtk_widget_show", FunctionDescriptor.ofVoid(ADDRESS));

    protected Widget(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public void initTemplate() {
        BindingSupport.runThrowing(() -> GTK_WIDGET_INIT_TEMPLATE.invoke(getMemoryAddress()));
    }

    public void setHalign(Align align) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_HALIGN.invoke(getMemoryAddress(), align.getValue()));
    }

    public void setHexpand(boolean expand) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_HEXPAND.invoke(getMemoryAddress(), expand));
    }

    public void setValign(Align align) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_VALIGN.invoke(getMemoryAddress(), align.getValue()));
    }

    public void setVexpand(boolean expand) {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SET_VEXPAND.invoke(getMemoryAddress(), expand));
    }

    public void show() {
        BindingSupport.runThrowing(() -> GTK_WIDGET_SHOW.invoke(getMemoryAddress()));
    }

    public static class Class extends InitiallyUnowned.Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                InitiallyUnowned.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("show"),
                ADDRESS.withName("hide"),
                ADDRESS.withName("map"),
                ADDRESS.withName("unmap"),
                ADDRESS.withName("realize"),
                ADDRESS.withName("unrealize"),
                ADDRESS.withName("root"),
                ADDRESS.withName("unroot"),
                ADDRESS.withName("size_allocate"),
                ADDRESS.withName("state_flags_changed"),
                ADDRESS.withName("direction_changed"),
                ADDRESS.withName("get_request_mode"),
                ADDRESS.withName("measure"),
                ADDRESS.withName("mnemonic_activate"),
                ADDRESS.withName("grab_focus"),
                ADDRESS.withName("focus"),
                ADDRESS.withName("set_focus_child"),
                ADDRESS.withName("move_focus"),
                ADDRESS.withName("keynav_failed"),
                ADDRESS.withName("query_tooltip"),
                ADDRESS.withName("compute_expand"),
                ADDRESS.withName("css_changed"),
                ADDRESS.withName("system_setting_changed"),
                ADDRESS.withName("snapshot"),
                ADDRESS.withName("contains"),
                ADDRESS.withName("priv"),
                MemoryLayout.sequenceLayout(8, GPOINTER).withName("padding"));

        private static final MethodHandle GTK_WIDGET_CLASS_BIND_TEMPLATE_CHILD_FULL = BindingSupport.lookup(
                "gtk_widget_class_bind_template_child_full",
                FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, JAVA_BOOLEAN, JAVA_LONG));
        private static final MethodHandle GTK_WIDGET_CLASS_SET_TEMPLATE =
                BindingSupport.lookup("gtk_widget_class_set_template", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }

        public void bindTemplateChild(String name, boolean internalChild, long structOffset) {
            BindingSupport.runThrowing(local -> GTK_WIDGET_CLASS_BIND_TEMPLATE_CHILD_FULL.invoke(
                    getMemoryAddress(), local.allocateUtf8String(name), internalChild, structOffset));
        }

        public void setTemplate(Bytes template) {
            BindingSupport.runThrowing(
                    () -> GTK_WIDGET_CLASS_SET_TEMPLATE.invoke(getMemoryAddress(), template.getMemoryAddress()));
        }

        public void setTemplateFromClasspathResource(java.lang.Class<?> clazz, String resource) throws IOException {
            setTemplate(Bytes.ofClasspathResource(clazz, resource));
        }
    }
}
