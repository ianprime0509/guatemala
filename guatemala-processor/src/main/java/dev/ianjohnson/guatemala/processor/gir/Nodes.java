package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Nodes {
    private Nodes() {}

    static Iterable<Node> children(Node node) {
        return () -> iterator(node.getChildNodes());
    }

    static Iterator<Node> iterator(NodeList nodeList) {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(i++);
            }
        };
    }

    static Stream<Node> streamChildren(Node node) {
        return stream(node.getChildNodes());
    }

    static Stream<Node> stream(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }
}
