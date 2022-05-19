package dev.ianjohnson.guatemala.gtk;

public enum Align {
    FILL(0),
    START(1),
    END(2),
    CENTER(3),
    BASELINE(4);

    private final int value;

    Align(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
