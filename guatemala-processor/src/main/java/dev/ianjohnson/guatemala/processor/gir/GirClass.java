package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public record GirClass(GirName name, String getType, List<GirField> fields) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "class".equals(element.getLocalName());
    }

    public static GirClass load(Element element, String ns) {
        GirName name = GirName.parse(element.getAttributeNS(null, "name"), ns);
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        List<GirField> fields = new ArrayList<>();
        Nodes.stream(element.getChildNodes()).forEach(child -> {
            if (child instanceof Element e) {
                if (GirField.canLoad(e)) {
                    fields.add(GirField.load(e, ns));
                }
            }
        });
        return new GirClass(name, getType, List.copyOf(fields));
    }
}
