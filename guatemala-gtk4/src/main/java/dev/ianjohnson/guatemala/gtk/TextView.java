package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.ClassType;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;

import static dev.ianjohnson.guatemala.glib.Types.GBOOLEAN;
import static dev.ianjohnson.guatemala.glib.Types.GPOINTER;
import static java.lang.foreign.ValueLayout.ADDRESS;

public class TextView extends Widget {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(Widget.LAYOUT.withName("parent_instance"), ADDRESS.withName("priv"));
    public static final ClassType<Class, TextView> TYPE =
            ClassType.ofTypeGetter("gtk_text_view_get_type", Class::new, TextView::new);

    private static final MethodHandle GTK_TEXT_VIEW_SET_CURSOR_VISIBLE =
            BindingSupport.lookup("gtk_text_view_set_cursor_visible", FunctionDescriptor.ofVoid(ADDRESS, GBOOLEAN));
    private static final MethodHandle GTK_TEXT_VIEW_SET_EDITABLE =
            BindingSupport.lookup("gtk_text_view_set_editable", FunctionDescriptor.ofVoid(ADDRESS, GBOOLEAN));

    protected TextView(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public void setCursorVisible(boolean cursorVisible) {
        BindingSupport.runThrowing(() -> GTK_TEXT_VIEW_SET_CURSOR_VISIBLE.invoke(address(), cursorVisible));
    }

    public void setEditable(boolean editable) {
        BindingSupport.runThrowing(() -> GTK_TEXT_VIEW_SET_EDITABLE.invoke(address(), editable));
    }

    public static class Class extends Widget.Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                Widget.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("move_cursor"),
                ADDRESS.withName("set_anchor"),
                ADDRESS.withName("insert_at_cursor"),
                ADDRESS.withName("delete_from_cursor"),
                ADDRESS.withName("backspace"),
                ADDRESS.withName("cut_clipboard"),
                ADDRESS.withName("copy_clipboard"),
                ADDRESS.withName("paste_clipboard"),
                ADDRESS.withName("toggle_overwrite"),
                ADDRESS.withName("create_buffer"),
                ADDRESS.withName("snapshot_layer"),
                ADDRESS.withName("extend_selection"),
                ADDRESS.withName("insert_emoji"),
                MemoryLayout.sequenceLayout(8, GPOINTER));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
