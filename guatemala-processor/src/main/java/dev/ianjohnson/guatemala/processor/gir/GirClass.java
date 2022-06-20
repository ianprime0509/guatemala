package dev.ianjohnson.guatemala.processor.gir;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public record GirClass(
        String name,
        String getType,
        @Nullable GirName parent,
        boolean isAbstract,
        boolean isFinal,
        String refFunc,
        String unrefFunc,
        List<GirField> fields,
        List<GirCallable> constructors,
        List<GirCallable> functions,
        List<GirCallable> methods) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "class".equals(element.getLocalName());
    }

    public static GirClass load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        String parentValue = element.getAttributeNS(null, "parent");
        GirName parent = !parentValue.isEmpty() ? GirName.parse(parentValue, ns) : null;
        boolean isAbstract = "1".equals(element.getAttributeNS(null, "abstract"));
        boolean isFinal = "1".equals(element.getAttributeNS(null, "final"));
        String refFunc = element.getAttributeNS(NS.GLIB, "ref-func");
        String unrefFunc = element.getAttributeNS(NS.GLIB, "unref-func");
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
        return new GirClass(
                name,
                getType,
                parent,
                isAbstract,
                isFinal,
                refFunc,
                unrefFunc,
                List.copyOf(fields),
                List.copyOf(constructors),
                List.copyOf(functions),
                List.copyOf(methods));
    }
}
