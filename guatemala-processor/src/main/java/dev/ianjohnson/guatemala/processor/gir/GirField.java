package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirField(String name, GirFieldType type) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "field".equals(element.getLocalName());
    }

    public static GirField load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        GirFieldType type = Nodes.children(element)
                .filter(child -> child instanceof Element e && GirFieldType.canLoad(e))
                .map(child -> GirFieldType.load((Element) child, ns))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("field must contain a type"));
        return new GirField(name, type);
    }
}
