package dev.ianjohnson.guatemala.core;

import java.util.Set;

public interface Flag {
    static int toInt(Set<? extends Flag> flags) {
        return flags.stream().mapToInt(Flag::getValue).reduce(0, (v1, v2) -> v1 | v2);
    }

    int getValue();
}
