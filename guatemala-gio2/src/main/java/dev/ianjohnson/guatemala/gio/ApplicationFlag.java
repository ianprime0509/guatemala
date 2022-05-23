package dev.ianjohnson.guatemala.gio;

import dev.ianjohnson.guatemala.core.Flag;

public enum ApplicationFlag implements Flag {
    IS_SERVICE(1),
    IS_LAUNCHER(2),
    HANDLES_OPEN(4),
    HANDLES_COMMAND_LINE(8),
    SEND_ENVIRONMENT(16),
    NON_UNIQUE(32),
    CAN_OVERRIDE_APP_ID(64),
    ALLOW_REPLACEMENT(128),
    REPLACE(256);

    private final int value;

    ApplicationFlag(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
