package dev.ianjohnson.guatemala.processor.gir;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

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
}
