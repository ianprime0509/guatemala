package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public record GirEnum(String name, String getType, List<GirMember> members, List<GirCallable> functions) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "enumeration".equals(element.getLocalName());
    }

    public static GirEnum load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        List<GirMember> members = new ArrayList<>();
        List<GirCallable> functions = new ArrayList<>();
        for (Node child : Nodes.children(element)) {
            if (child instanceof Element e) {
                if (GirMember.canLoad(e)) {
                    members.add(GirMember.load(e));
                } else if (GirCallable.canLoadFunction(e)) {
                    functions.add(GirCallable.load(e, ns));
                }
            }
        }
        return new GirEnum(name, getType, List.copyOf(members), List.copyOf(functions));
    }
}
