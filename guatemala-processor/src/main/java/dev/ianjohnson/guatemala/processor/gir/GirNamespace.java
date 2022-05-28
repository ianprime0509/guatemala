package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GirNamespace(String name, Map<String, GirClass> classes, Map<String, GirRecord> records) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "namespace".equals(element.getLocalName());
    }

    public static GirNamespace load(Element namespace) {
        String name = namespace.getAttributeNS(null, "name");
        Map<String, GirClass> classes = new HashMap<>();
        Map<String, GirRecord> records = new HashMap<>();
        Nodes.stream(namespace.getChildNodes()).forEach(child -> {
            if (child instanceof Element e) {
                if (GirClass.canLoad(e)) {
                    GirClass c = GirClass.load(e, name);
                    classes.put(c.name().local(), c);
                } else if (GirRecord.canLoad(e)) {
                    GirRecord r = GirRecord.load(e, name);
                    records.put(r.name().local(), r);
                }
            }
        });
        return new GirNamespace(name, Map.copyOf(classes), Map.copyOf(records));
    }
}
