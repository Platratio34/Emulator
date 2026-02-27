package com.peter.emulator.lang.tokens;

import java.util.Map;

import com.peter.emulator.lang.Location;

public class StringToken extends Token {
    public String value = "";
    public boolean ch = false;
    public boolean escape = false;
    public boolean closed = false;

    protected static final Map<Character, String> escapes = Map.of(
        '\\', "\\",
        'n', "\n",
        't', "\t",
        '"', "\"",
        '\'', "\'",
        '0', "\0"
    );

    public StringToken(char c, Location location) {
        super(location);
        ch = c == '\'';
    }

    @Override
    public Token ingest(char c, Location location) {
        if (closed)
            return null;
        endLocation = location;
        if(escape) {
            if(escapes.containsKey(c)) {
                value += escapes.get(c);
            } else {
                value += c;
            }
            escape = false;
            return this;
        } else if (c == '\'' && ch) {
            closed = true;
            return this;
        } else if (c == '"' && !ch) {
            closed = true;
            return this;
        } else if (c == '\\') {
            escape = true;
            return this;
        }
        value += c;
        return this;
    }

    public String escapedValue() {
        return value.replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r").replace("\"","\\\"").replace("\'","\\'");
    }

    @Override
    public String toString() {
        return String.format("StringToken{value=\"%s\", ch=%s, closed=%s, %s}", escapedValue(), ch ? "true" : "false",
                closed ? "true" : "false", startLocation);
    }

    @Override
    public String debugString() {
        return String.format(ch ? "'%s'" : "\"%s\"", escapedValue());
    }
}