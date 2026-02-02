package com.peter.emulator.lang;

public record Location(String file, int line, int col) {

    @Override
    public final String toString() {
        return String.format("%s: %d:%d", file, line, col);
    }

    public Span span(Location end) {
        return new Span(this, end);
    }

    public Span span() {
        return new Span(this, null);
    }
}
