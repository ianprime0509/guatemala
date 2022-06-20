package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.ClassName;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public record GirName(@Nullable String ns, String local) {
    private static final Pattern DOT = Pattern.compile("\\.");
    private static final Pattern PRIMITIVE_START = Pattern.compile("[a-z]");

    public static GirName parse(String name, String defaultNs) {
        String[] parts = DOT.split(name, 2);
        if (parts.length == 1) {
            return new GirName(PRIMITIVE_START.matcher(parts[0]).lookingAt() ? null : defaultNs, parts[0]);
        } else {
            return new GirName(parts[0], parts[1]);
        }
    }

    public ClassName className(CodegenContext ctx) {
        if (ns() == null) {
            throw new IllegalStateException("Cannot get class name for non-namespaced type " + local());
        }
        String nsPackage = ctx.namespacePackages().get(ns());
        if (nsPackage == null) {
            throw new IllegalStateException("No package mapping for namespace " + ns());
        }
        return ClassName.get(nsPackage, local());
    }

    @Override
    public String toString() {
        return ns == null ? local : ns + "." + local;
    }
}
