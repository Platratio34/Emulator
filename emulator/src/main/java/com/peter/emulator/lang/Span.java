package com.peter.emulator.lang;

public record Span(Location start, Location end) {

    public Span withEnd(Location end) {
        return new Span(start, end);
    }
}
