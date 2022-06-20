package dev.ianjohnson.guatemala.processor;

import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.processor.gir.GirCallable;

public final class Impls {
    private Impls() {}

    public static void addCallables(
            TypeSpec.Builder typeBuilder, CodegenContext ctx, Iterable<? extends GirCallable> callables) {
        for (GirCallable callable : callables) {
            if (callable.name().isEmpty() || callable.isVariadic()) {
                continue;
            }
            GirCallable.Impl impl = callable.impl(ctx);
            typeBuilder.addField(impl.methodHandleField());
            typeBuilder.addMethod(impl.method());
        }
    }
}
