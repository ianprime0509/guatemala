package dev.ianjohnson.guatemala.processor.gir;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record GirCallable(
        String name, String cIdentifier, ReturnValue returnValue, List<Parameter> parameters) {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "callable".equals(element.getLocalName());
    }

    public static boolean canLoadConstructor(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "constructor".equals(element.getLocalName());
    }

    public static boolean canLoadMethod(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "method".equals(element.getLocalName());
    }

    public static boolean canLoadFunction(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "function".equals(element.getLocalName());
    }

    public static GirCallable load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String cIdentifier = element.getAttributeNS(NS.C, "identifier");
        List<Parameter> parameters = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && Parameters.canLoad(e))
                .map(child -> Parameters.load((Element) child, ns).parameters())
                .findFirst()
                .orElse(List.of());
        ReturnValue returnValue = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && ReturnValue.canLoad(e))
                .map(child -> ReturnValue.load((Element) child, ns))
                .findFirst()
                .orElse(null);
        return new GirCallable(name, cIdentifier, returnValue, parameters);
    }

    public record ReturnValue(GirAnyType type) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI()) && "return-value".equals(element.getLocalName());
        }

        public static ReturnValue load(Element element, String ns) {
            GirAnyType type = Nodes.streamChildren(element)
                    .filter(child -> child instanceof Element e && GirAnyType.canLoad(e))
                    .map(child -> GirAnyType.load((Element) child, ns))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("return-value must contain a type"));
            return new ReturnValue(type);
        }
    }

    public record Parameter(String name, GirAnyType type, boolean isInstance) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI())
                    && ("parameter".equals(element.getLocalName())
                            || "instance-parameter".equals(element.getLocalName()));
        }

        public static Parameter load(Element element, String ns) {
            String name = element.getAttributeNS(null, "name");
            GirAnyType type = Nodes.streamChildren(element)
                    .flatMap(child -> {
                        if (child instanceof Element e) {
                            if (GirAnyType.canLoad(e)) {
                                return Stream.of(GirAnyType.load(e, ns));
                            } else if (NS.CORE.equals(e.getNamespaceURI()) && "varargs".equals(e.getLocalName())) {
                                return Stream.of(GirType.VARARGS);
                            }
                        }
                        return Stream.empty();
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("parameter must contain a type"));
            boolean isInstance = "instance-parameter".equals(element.getLocalName());
            return new Parameter(name, type, isInstance);
        }
    }

    private record Parameters(List<Parameter> parameters) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI()) && "parameters".equals(element.getLocalName());
        }

        public static Parameters load(Element element, String ns) {
            List<Parameter> parameters = Nodes.streamChildren(element)
                    .filter(child -> child instanceof Element e && Parameter.canLoad(e))
                    .map(child -> Parameter.load((Element) child, ns))
                    .toList();
            return new Parameters(parameters);
        }
    }
}
