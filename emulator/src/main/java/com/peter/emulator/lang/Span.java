package com.peter.emulator.lang;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public record Span(Location start, Location end) {

    public Span withEnd(Location end) {
        return new Span(start, end);
    }

    public Range toRange() {
        if(end == null) {
            return new Range(
                new Position(start.line()-1, start.col()),
                new Position(start.line()-1, start.col())
            );
        }
        return new Range(
            new Position(start.line()-1, start.col()),
            new Position(end.line()-1, end.col())
        );
    }
}
