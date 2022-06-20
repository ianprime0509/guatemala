package dev.ianjohnson.guatemala.core;

public interface BitField {
    static int toInt(Iterable<? extends BitField> flags) {
        long value = toLong(flags);
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Combined flags do not fit in an int: " + value);
        }
        return (int) value;
    }

    static long toLong(Iterable<? extends BitField> flags) {
        long value = 0;
        for (BitField flag : flags) {
            value = value | flag.value();
        }
        return value;
    }

    long value();
}
