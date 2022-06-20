package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.w3c.dom.Element;

public record GirField(String name, Type type) implements Named, Layoutable {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "field".equals(element.getLocalName());
    }

    public static GirField load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        Type type = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && Type.canLoad(e))
                .map(child -> Type.load((Element) child, ns))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("field must contain a type"));
        return new GirField(name, type);
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        return CodeBlock.of("$L.withName($S)", type().memoryLayout(ctx), name());
    }

    public sealed interface Type extends Layoutable permits GirCallback, GirAnyType {
        static boolean canLoad(Element element) {
            return GirCallback.canLoad(element) || GirAnyType.canLoad(element);
        }

        static Type load(Element element, String ns) {
            if (GirCallback.canLoad(element)) {
                return GirCallback.load(element, ns);
            } else {
                return GirAnyType.load(element, ns);
            }
        }
    }
}
