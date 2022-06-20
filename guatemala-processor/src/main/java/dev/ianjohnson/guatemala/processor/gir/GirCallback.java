package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirCallback(String name) implements GirField.Type {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "callback".equals(element.getLocalName());
    }

    public static GirCallback load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        return new GirCallback(name);
    }
}
