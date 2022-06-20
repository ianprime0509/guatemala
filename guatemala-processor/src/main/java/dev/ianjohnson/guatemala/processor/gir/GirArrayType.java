package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Type;

public record GirArrayType(GirAnyType elementType, @Nullable Long fixedSize) implements GirAnyType {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "array".equals(element.getLocalName());
    }

    public static GirArrayType load(Element element, String ns) {
        GirAnyType elementType = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && GirAnyType.canLoad(e))
                .map(child -> GirAnyType.load((Element) child, ns))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("array must contain a type"));
        String fixedSizeValue = element.getAttributeNS(null, "fixed-size");
        Long fixedSize = fixedSizeValue.isEmpty() ? null : Long.parseLong(fixedSizeValue);
        return new GirArrayType(elementType, fixedSize);
    }

    @Override
    public Type impl(CodegenContext ctx) {
        return fixedSize() != null ? MemorySegment.class : MemoryAddress.class;
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        if (fixedSize() != null) {
            return CodeBlock.of(
                    "$T.sequenceLayout($L, $L)",
                    MemoryLayout.class,
                    fixedSize(),
                    elementType().memoryLayout(ctx));
        } else {
            return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
        }
    }
}
