package dev.ianjohnson.guatemala.annotation;

import java.lang.annotation.Target;

@Target({})
public @interface NamespaceDependency {
    String value();

    String packageName();
}
