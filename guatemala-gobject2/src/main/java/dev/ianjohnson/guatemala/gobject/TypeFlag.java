package dev.ianjohnson.guatemala.gobject;

import java.util.Set;

public enum TypeFlag {
    ABSTRACT(16),
    VALUE_ABSTRACT(32),
    FINAL(64);

    private final int value;

    TypeFlag(int value) {
        this.value = value;
    }

    public static int toInt(Set<TypeFlag> flags) {
        int value = 0;
        for (TypeFlag flag : flags) {
            value |= flag.getValue();
        }
        return value;
    }

    public int getValue() {
        return value;
    }
}
