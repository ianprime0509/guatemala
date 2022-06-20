package dev.ianjohnson.guatemala.processor;

import dev.ianjohnson.guatemala.processor.gir.GirNamespace;

import javax.lang.model.element.PackageElement;
import java.util.Map;

public record CodegenContext(
        Map<String, GirNamespace> namespaces,
        Map<String, String> namespacePackages,
        GirNamespace activeNamespace,
        PackageElement activePackage) {
    public CodegenContext {
        namespaces = Map.copyOf(namespaces);
        namespacePackages = Map.copyOf(namespacePackages);
    }
}
