package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.ELSymbol.Type;

public class DocCommentToken extends Token {

    public Location end;
    private int closeChar = 0;

    public String value = "";

    protected DocCommentToken(Location location) {
        super(location);
    }

    private char tempC = ' ';
    @Override
    public Token ingest(char c, Location location) {
        if (closeChar == 2) {
            return null;
        }
        endLocation = location;
        if (closeChar == 1) {
            if (closeChar == 1 && c == '@') {
                closeChar = 2;
                value = value.trim();
                return this;
            } else {
                closeChar = 0;
                value += tempC;
            }
        }
        if (closeChar == 0 && c == '/') {
            closeChar = 1;
            tempC = c;
            return this;
        }
        value += c;
        return this;
    }

    @Override
    public String debugString() {
        return "@/ " + value + " /@";
    }

    @Override
    public String toString() {
        return String.format("DocCommentToken{closeChar=%d,value=\"%s\"}", closeChar, value);
    }

    public void addSymbols(ProgramUnit unit) {
        unit.addSymbol(new ELSymbol(Type.COMMENT_DOC, span()));
    }

}
