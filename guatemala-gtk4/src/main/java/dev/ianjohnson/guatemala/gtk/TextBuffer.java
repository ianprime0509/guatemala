package dev.ianjohnson.guatemala.gtk;

import dev.ianjohnson.guatemala.core.BindingSupport;
import dev.ianjohnson.guatemala.gobject.Object;
import dev.ianjohnson.guatemala.gobject.ObjectType;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class TextBuffer extends Object {
    public static final MemoryLayout LAYOUT =
            BindingSupport.structLayout(Object.LAYOUT.withName("parent_instance"), ADDRESS.withName("priv"));
    public static final ObjectType<Class, TextBuffer> TYPE =
            ObjectType.ofTypeGetter("gtk_text_buffer_get_type", Class::new, TextBuffer::new);

    protected TextBuffer(MemoryAddress memoryAddress) {
        super(memoryAddress);
    }

    public static class Class extends Object.Class {
        public static final MemoryLayout LAYOUT = BindingSupport.structLayout(
                Object.Class.LAYOUT.withName("parent_class"),
                ADDRESS.withName("insert_text"),
                ADDRESS.withName("insert_paintable"),
                ADDRESS.withName("insert_child_anchor"),
                ADDRESS.withName("delete_range"),
                ADDRESS.withName("changed"),
                ADDRESS.withName("modified_changed"),
                ADDRESS.withName("mark_set"),
                ADDRESS.withName("mark_deleted"),
                ADDRESS.withName("apply_tag"),
                ADDRESS.withName("remove_tag"),
                ADDRESS.withName("begin_user_action"),
                ADDRESS.withName("end_user_action"),
                ADDRESS.withName("paste_done"),
                ADDRESS.withName("undo"),
                ADDRESS.withName("redo"),
                MemoryLayout.sequenceLayout(4, ADDRESS));

        protected Class(MemoryAddress memoryAddress) {
            super(memoryAddress);
        }
    }
}
