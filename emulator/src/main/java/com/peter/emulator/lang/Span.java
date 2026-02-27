package com.peter.emulator.lang;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.peter.emulator.languageserver.ELLanguageServer;

public record Span(Location start, Location end) {

    public Span withEnd(Location end) {
        return new Span(start, end);
    }

    @Override
    public final String toString() {
        if (end != null)
            return String.format("Span{start={%s}, end={%s}}", start, end);
        return String.format("Span{start={%s}, end=null}", start);
    }
    
    public final String debugString() {
        return String.format("%d:%d - %d:%d", start.line(), start.col(), end.line(), end.col());
    } 

    public Range toRange() {
        if(end == null) {
            return new Range(
                new Position(start.line()-1, start.col()-2),
                new Position(start.line()-1, start.col()-1)
            );
        }
        return new Range(
            new Position(start.line()-1, start.col()-2),
            new Position(end.line()-1, end.col()-1)
        );
    }

    public boolean contains(int line, int col, ELLanguageServer lServer) {
        if(lServer != null)
            lServer.logDebug("Checking %s for %d:%d", this, line, col);
        if (end == null)
            return start.line() == line && start.col() == col;
        return start.line() <= line && end.line() >= line && start.col() <= col && end.col() >= col;
    }
    

    public boolean contains(Position position, ELLanguageServer lServer) {
        return contains(position.getLine() + 1, position.getCharacter()+2, lServer);
    }
}
