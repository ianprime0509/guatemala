package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class GirStore {

    private final DocumentBuilder documentBuilder;
    private final Path directory;
    private final Set<String> loaded = new HashSet<>();
    private final Map<String, GirNamespace> namespaces = new HashMap<>();

    public GirStore(Path directory) {
        try {
            this.documentBuilder = DocumentBuilderFactory.newDefaultNSInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        this.directory = directory;
    }

    public Optional<GirNamespace> namespace(String name) {
        return Optional.ofNullable(namespaces.get(name));
    }

    public void load(String file) throws IOException {
        if (!loaded.add(file)) {
            return;
        }

        Document document;
        try {
            document = documentBuilder.parse(directory.resolve(file).toFile());
        } catch (SAXException e) {
            throw new IOException("Invalid XML in GIR file: " + file, e);
        }

        NodeList children = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element e && GirNamespace.canLoad(e)) {
                GirNamespace ns = GirNamespace.load(e);
                namespaces.put(ns.name(), ns);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        GirStore store = new GirStore(Path.of("guatemala-gtk4/src/main/gir"));
        store.load("Gtk-4.0.gir");
    }
}
