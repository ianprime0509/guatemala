package dev.ianjohnson.guatemala.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface NamespaceBinding {
    String value();

    NamespaceDependency[] dependencies() default {};
}
