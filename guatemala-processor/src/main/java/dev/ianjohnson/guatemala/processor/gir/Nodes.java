package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Nodes {
    private Nodes() {}

    static Stream<Node> children(Node node) {
        return stream(node.getChildNodes());
    }

    static Stream<Node> stream(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }
}
