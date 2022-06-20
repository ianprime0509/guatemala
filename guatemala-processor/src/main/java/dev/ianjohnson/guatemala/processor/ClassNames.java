package dev.ianjohnson.guatemala.processor;

import com.squareup.javapoet.ClassName;

public final class ClassNames {
    public static final ClassName Addressable = ClassName.get("dev.ianjohnson.guatemala.glib", "Addressable");
    public static final ClassName BindingSupport = ClassName.get("dev.ianjohnson.guatemala.core", "BindingSupport");
    public static final ClassName BitField = ClassName.get("dev.ianjohnson.guatemala.core", "BitField");
    public static final ClassName ClassType = ClassName.get("dev.ianjohnson.guatemala.gobject", "ClassType");
    public static final ClassName Enumeration = ClassName.get("dev.ianjohnson.guatemala.core", "Enumeration");
    public static final ClassName ReferenceCounted = ClassName.get("dev.ianjohnson.guatemala.glib", "ReferenceCounted");
    public static final ClassName Types = ClassName.get("dev.ianjohnson.guatemala.glib", "Types");

    private ClassNames() {}
}
