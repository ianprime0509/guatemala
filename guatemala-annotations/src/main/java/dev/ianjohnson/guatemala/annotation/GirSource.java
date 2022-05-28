package dev.ianjohnson.guatemala.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
@Repeatable(GirSources.class)
public @interface GirSource {
    String value();
}
