package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record GirRepository(Map<String, GirNamespace> namespaces) {
    public GirRepository {
        namespaces = Map.copyOf(namespaces);
    }

    public static GirRepository load(Element element) {
        Map<String, GirNamespace> namespaces = new HashMap<>();
        for (Node child : Nodes.children(element)) {
            if (child instanceof Element e && GirNamespace.canLoad(e)) {
                GirNamespace loaded = GirNamespace.load(e);
                namespaces.put(loaded.name(), loaded);
            }
        }
        return new GirRepository(namespaces);
    }

    public static GirRepository load(Path path) throws IOException {
        Document document;
        try {
            document = DocumentBuilderFactory.newDefaultNSInstance()
                    .newDocumentBuilder()
                    .parse(path.toFile());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IOException("Invalid XML in GIR file: " + path, e);
        }
        return load(document.getDocumentElement());
    }
}
