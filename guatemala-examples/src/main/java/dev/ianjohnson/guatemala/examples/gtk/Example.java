package dev.ianjohnson.guatemala.examples.gtk;

public @interface Example {
    String name();

    String description();

    String source() default "";
}
