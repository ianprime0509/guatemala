package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirMember(String name, long value) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "member".equals(element.getLocalName());
    }

    public static GirMember load(Element element) {
        String name = element.getAttributeNS(null, "name");
        long value = Long.parseLong(element.getAttributeNS(null, "value"));
        return new GirMember(name, value);
    }
}
