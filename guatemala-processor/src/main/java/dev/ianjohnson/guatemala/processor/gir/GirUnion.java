package dev.ianjohnson.guatemala.processor.gir;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public record GirUnion(
        String name,
        @Nullable String getType,
        List<GirField> fields,
        List<GirCallable> constructors,
        List<GirCallable> functions,
        List<GirCallable> methods) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "union".equals(element.getLocalName());
    }

    public static GirUnion load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        if (getType.isEmpty()) {
            getType = null;
        }
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
        return new GirUnion(
                name,
                getType,
                List.copyOf(fields),
                List.copyOf(constructors),
                List.copyOf(functions),
                List.copyOf(methods));
    }
}
