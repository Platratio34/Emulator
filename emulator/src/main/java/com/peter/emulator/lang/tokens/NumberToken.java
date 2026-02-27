package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.Location;

public class NumberToken extends Token {
    public String value;
    public boolean hex;
    public boolean bin;
    public int numValue = 0;

    public NumberToken(char c, Location location) {
        super(location);
        value = c + "";
        numValue = (int)Long.parseLong(value, 10);
    }

    @Override
    public Token ingest(char c, Location location) {
        if (c == 'x') {
            if (value.length() == 1) {
                hex = true;
                value = "";
                numValue = 0;
                endLocation = location;
                return this;
            }
            return null;
        } else if (c == 'b') {
            if (value.length() == 1) {
                bin = true;
                value = "";
                numValue = 0;
                endLocation = location;
                return this;
            }
            return null;
        }
        if (hex) {
            if (!(Character.isDigit(c) || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || c == '_'))
                return null;
        } else if (bin) {
            if (!(c == '0' || c == '1' || c == '_'))
                return null;
        } else {
            if (!(Character.isDigit(c) || c == '.' || c == 'e' || c == '_'))
                return null;
        }
        value += c;
        endLocation = location;
        numValue = (int)Long.parseLong(value.replace("_",""), bin ? 2 : (hex ? 16 : 10));
        return this;
    }
    @Override
    public String toString() {
        return String.format("NumberToken{value=%s, hex=%s, bin=%s, %s}", value, hex ? "true" : "false",
                bin ? "true" : "false", startLocation);
    }

    @Override
    public String debugString() {
        String out = "";
        if (hex)
            out = "0x";
        if (bin)
            out = "0b";
        return out + value;
    }
}