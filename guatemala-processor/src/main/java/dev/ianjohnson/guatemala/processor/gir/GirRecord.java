package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public record GirRecord(GirName name, List<GirField> fields) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "record".equals(element.getLocalName());
    }

    public static GirRecord load(Element element, String ns) {
        GirName name = GirName.parse(element.getAttributeNS(null, "name"), ns);
        List<GirField> fields = new ArrayList<>();
        Nodes.stream(element.getChildNodes()).forEach(child -> {
            if (!(child instanceof Element e)) {
                return;
            }

            if (GirField.canLoad(e)) {
                fields.add(GirField.load(e, ns));
            }
        });
        return new GirRecord(name, List.copyOf(fields));
    }
}
