package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public sealed interface GirFieldType permits GirCallback, GirAnyType {
    static boolean canLoad(Element element) {
        return GirCallback.canLoad(element) || GirAnyType.canLoad(element);
    }

    static GirFieldType load(Element element, String ns) {
        if (GirCallback.canLoad(element)) {
            return GirCallback.load(element, ns);
        } else {
            return GirAnyType.load(element, ns);
        }
    }
}
