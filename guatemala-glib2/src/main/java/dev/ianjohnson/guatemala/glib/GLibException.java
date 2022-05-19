package dev.ianjohnson.guatemala.glib;

public class GLibException extends RuntimeException {
    private final Error error;

    public GLibException(Error error) {
        super(error.getMessage());
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
