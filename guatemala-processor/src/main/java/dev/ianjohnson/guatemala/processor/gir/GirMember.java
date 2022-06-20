package dev.ianjohnson.guatemala.processor.gir;

import org.w3c.dom.Element;

public record GirMember(String name, int value) implements Named {
    private static final long MAX_UNSIGNED_INT = (1L << 32) - 1;

    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "member".equals(element.getLocalName());
    }

    public static GirMember load(Element element) {
        String name = element.getAttributeNS(null, "name");
        long longValue = Long.parseLong(element.getAttributeNS(null, "value"));
        int value;
        if (longValue < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Value for member " + name + " is less than the smallest integer");
        } else if (longValue < Integer.MAX_VALUE) {
            value = (int) longValue;
        } else if (longValue < MAX_UNSIGNED_INT) {
            value = (int) -(MAX_UNSIGNED_INT - longValue);
        } else {
            throw new IllegalArgumentException("Value for member " + name + " is greater than the largest integer");
        }
        return new GirMember(name, value);
    }
}
