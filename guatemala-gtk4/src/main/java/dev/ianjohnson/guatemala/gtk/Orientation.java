package dev.ianjohnson.guatemala.gtk;

public enum Orientation {
    HORIZONTAL(0),
    VERTICAL(1);

    private final int value;

    Orientation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
