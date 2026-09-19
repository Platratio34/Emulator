package com.peter.emulator.lang.tokens;

import java.util.Map;

import com.peter.emulator.lang.Location;

public class StringToken extends Token {
    public String value = "";
    public String raw = "";
    public boolean ch = false;
    public boolean escape = false;
    public boolean closed = false;
    public boolean block = false;

    protected static final Map<Character, String> escapes = Map.of(
        '\\', "\\",
        'n', "\n",
        't', "\t",
        '"', "\"",
        '\'', "\'",
        '0', "\0"
    );
    protected static final Map<Character, String> blockEscapes = Map.of(
        '\\', "\\",
        't', "\t",
        '0', "\0"
    );

    public StringToken(char c, Location location) {
        super(location);
        ch = c == '\'';
    }

    int closeCount = 0;
    boolean leadWS = false;
    @Override
    public Token ingest(char c, Location location) {
        if (closed) {
            if (!ch && !block && c == '"') {
                closed = false;
                block = true;
                closeCount = -1;
                raw += '"';
            } else {
                return null;
            }
        }
        endLocation = location;
        raw += c;
        if (c == '\r') {
            return this;
        }
        if (block) {
            if (leadWS && (c == '\t' || c == ' ')) {
                closeCount = 0;
                return this;
            } else {
                leadWS = c == '\n';
                if (leadWS && value.length() == 0) {
                    closeCount = 0;
                    return this;
                }
            }
        }
        if(escape) {
            closeCount = 0;
            if (block) {
                value += blockEscapes.getOrDefault(c, "\\" + c);
            } else {
                value += escapes.getOrDefault(c, c+"");
            }
            escape = false;
            return this;
        } else if (c == '\'' && ch) {
            closed = true;
            return this;
        } else if (c == '"' && !ch) {
            if (block) {
                if (closeCount == -1) {
                    closeCount = 0;
                    return this;
                }
                closeCount++;
                if (closeCount == 3) {
                    closed = true;
                    if (value.charAt(value.length() - 3) == '\n') {
                        value = value.substring(0, value.length() - 3);
                    } else {
                        value = value.substring(0, value.length() - 2);
                    }
                    return this;
                }
            } else {
                closed = true;
                return this;
            }
        } else if (c == '\\') {
            escape = true;
            return this;
        }
        value += c;
        return this;
    }

    public String escapedValue() {
        return value.replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r").replace("\"","\\\"").replace("\'","\\'").replace("\0","\\0");
    }

    @Override
    public String toString() {
        return String.format("StringToken{value=\"%s\", ch=%s, closed=%s, %s}", escapedValue(), ch ? "true" : "false",
                closed ? "true" : "false", startLocation);
    }

    @Override
    public String debugString() {
        return String.format(block ? "\"\"\"%s\"\"\"" : (ch ? "'%s'" : "\"%s\""), escapedValue());
    }
}