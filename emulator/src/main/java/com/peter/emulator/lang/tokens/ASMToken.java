package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.ELSymbol.Type;

public class ASMToken extends Token {
    public boolean closed = false;

    public String raw = "";
    public String value = "";

    public Location bodyStart = null;

    protected ASMToken(Location location) {
        super(location);
    }


    private boolean rmWS = true;
    @Override
    public Token ingest(char c, Location location) {
        if (closed) {
            return null;
        }
        if (bodyStart == null) {
            bodyStart = location;
        }
        if (c == '}') {
            closed = true;
            endLocation = location;
            return this;
        }
        raw += c;
        if (c == '\r') {
            endLocation = location;
            return this;
        }
        if (rmWS && (c == ' ' || c == '\t')) {
            endLocation = location;
            return this;
        } else {
            rmWS = c == '\n';
        }
        value += c;
        endLocation = location;
        return this;
    }

    @Override
    public String debugString() {
        return String.format("asm{%s}", value.replaceAll("\\\\","\\\\").replaceAll("\n","\\\\n").replaceAll("\r",""));
    }

    public void addSymbols(ProgramUnit unit) {
        unit.addSymbol(Type.KEYWORD, startLocation.span(bodyStart.add(-1)));
        unit.addSymbol(Type.STRING_LITERAL, bodyStart.span(endLocation));
    }

}
