package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public sealed interface GirAnyType extends GirFieldType permits GirType, GirArrayType {
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
}
