package com.peter.emulator.lang.tokens;

import java.util.ArrayList;

import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Span;

public abstract class Token {

    public final Location startLocation;
    public Location endLocation;
    public ArrayList<Token> subTokens;

    public abstract Token ingest(char c, Location location);
    
    protected Token(Location location) {
        startLocation = location;
        endLocation = location;
        if (location == null)
            throw new NullPointerException("Start location must be non-null");
    }

    public boolean hasSub() {
        return subTokens != null && !subTokens.isEmpty();
    }

    public int subSize() {
        return (subTokens == null) ? 0 : subTokens.size();
    }

    public Token subFirst() {
        return (subTokens == null) ? null : subTokens.getFirst();
    }
    public Token subLast() {
        return (subTokens == null) ? null : subTokens.getLast();
    }

    public abstract String debugString();

    public boolean wsBefore() {
        return true;
    }

    public Span span() {
        return new Span(startLocation, endLocation);
    }
}
