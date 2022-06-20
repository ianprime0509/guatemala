package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirAlias(String name, GirType type) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "alias".equals(element.getLocalName());
    }

    public static GirAlias load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        GirType type = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && GirType.canLoad(e))
                .map(child -> GirType.load((Element) child, ns))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("alias must contain a type"));
        return new GirAlias(name, type);
    }
}
