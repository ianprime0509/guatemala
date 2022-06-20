package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.w3c.dom.Element;

import java.lang.foreign.Addressable;
import java.lang.foreign.MemoryAddress;
import java.lang.reflect.Type;

public sealed interface GirAnyType extends GirField.Type, Layoutable permits GirType, GirArrayType {
    static boolean canLoad(Element element) {
        return GirType.canLoad(element) || GirArrayType.canLoad(element);
    }

    static GirAnyType load(Element element, String ns) {
        if (GirArrayType.canLoad(element)) {
            return GirArrayType.load(element, ns);
        } else {
            return GirType.load(element, ns);
        }
    }

    Type impl(CodegenContext ctx);

    default Type paramImpl(CodegenContext ctx) {
        Type binding = impl(ctx);
        return binding == MemoryAddress.class ? Addressable.class : binding;
    }
}
