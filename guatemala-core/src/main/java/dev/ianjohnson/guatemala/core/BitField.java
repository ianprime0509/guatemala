package dev.ianjohnson.guatemala.core;

public interface BitField {
    static int toInt(Iterable<? extends BitField> flags) {
        int value = 0;
        for (BitField flag : flags) {
            value = value | flag.value();
        }
        return value;
    }

    int value();
}
