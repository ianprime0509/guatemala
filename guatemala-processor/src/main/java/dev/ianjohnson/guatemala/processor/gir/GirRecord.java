package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public record GirRecord(
        GirName name,
        List<GirField> fields,
        List<GirCallable> constructors,
        List<GirCallable> functions,
        List<GirCallable> methods) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "record".equals(element.getLocalName());
    }

    public static GirRecord load(Element element, String ns) {
        GirName name = GirName.parse(element.getAttributeNS(null, "name"), ns);
        List<GirField> fields = new ArrayList<>();
        List<GirCallable> constructors = new ArrayList<>();
        List<GirCallable> functions = new ArrayList<>();
        List<GirCallable> methods = new ArrayList<>();
        for (Node child : Nodes.children(element)) {
            if (child instanceof Element e) {
                if (GirField.canLoad(e)) {
                    fields.add(GirField.load(e, ns));
                } else if (GirCallable.canLoadConstructor(e)) {
                    constructors.add(GirCallable.load(e, ns));
                } else if (GirCallable.canLoadFunction(e)) {
                    functions.add(GirCallable.load(e, ns));
                } else if (GirCallable.canLoadMethod(e)) {
                    methods.add(GirCallable.load(e, ns));
                }
            }
        }
        return new GirRecord(
                name, List.copyOf(fields), List.copyOf(constructors), List.copyOf(functions), List.copyOf(methods));
    }
}
