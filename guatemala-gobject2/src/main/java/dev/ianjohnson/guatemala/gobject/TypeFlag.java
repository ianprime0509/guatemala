package dev.ianjohnson.guatemala.gobject;

import dev.ianjohnson.guatemala.core.Flag;

public enum TypeFlag implements Flag {
    ABSTRACT(16),
    VALUE_ABSTRACT(32),
    FINAL(64);

    private final int value;

    TypeFlag(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
