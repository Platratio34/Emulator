package com.peter.emulator.lang;

public record Location(String file, int line, int col) {

    @Override
    public final String toString() {
        return String.format("%s: %d:%d", file, line, col);
    }
}
