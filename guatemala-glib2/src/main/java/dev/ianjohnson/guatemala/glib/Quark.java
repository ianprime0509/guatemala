package dev.ianjohnson.guatemala.glib;

public final class Quark {
    private final int raw;

    private Quark(int raw) {
        this.raw = raw;
    }

    public static Quark ofRaw(int raw) {
        return new Quark(raw);
    }

    public int getRaw() {
        return raw;
    }

    public boolean isValid() {
        return raw != 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quark other && other.raw == raw;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(raw);
    }

    @Override
    public String toString() {
        return "Quark(" + Integer.toUnsignedString(raw) + ")";
    }
}
