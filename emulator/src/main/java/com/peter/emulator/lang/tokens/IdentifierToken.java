package com.peter.emulator.lang.tokens;

import java.util.ArrayList;

import com.peter.emulator.lang.Identifier;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Span;

public class IdentifierToken extends Token {

    public String value;
    public SetToken index = null;
    public SetToken params = null;
    protected boolean indexClosed = false;

    public IdentifierToken(char c, Location location) {
        super(location);
        value = c + "";
    }

    private boolean nextIsID = false;
    private boolean nextIsDot = false;
    private IdentifierToken nextId = null;
    
    @Override
    public IdentifierToken ingest(char c, Location location) {
        if (nextId != null) {
            IdentifierToken tkn = nextId.ingest(c, location);
            if (tkn != null) {
                endLocation = location;
                nextId = tkn;
                return this;
            } else {
                subTokens.add(nextId);
            }
            return null;
        } else if (index != null && !index.closed) {
            SetToken tkn = index.ingest(c, location);
            if (tkn != null) {
                endLocation = location;
                index = tkn;
                return this;
            }
        } else if(params != null && !params.closed) {
            SetToken tkn = params.ingest(c, location);
            if (tkn != null) {
                endLocation = location;
                params = tkn;
                return this;
            }
        }
        if (nextIsID) {
            nextIsID = false;
            if (validStart(c)) {
                nextId = new IdentifierToken(c, location);
                subTokens = new ArrayList<>();
                // subTokens.add(nextId);
                endLocation = location;
                return this;
            } else {
                throw new TokenizerError("Invalid identifier");
            }
        } else if (validStart(c) || Character.isDigit(c)) {
            if (nextIsDot)
                throw new TokenizerError("Unexpected character in identifier, expected `.`");
            value += c;
            endLocation = location;
            return this;
        } else if (c == '.') {
            nextIsID = true;
            endLocation = location;
            nextIsDot = false;
            return this;
        } else if (c == '[') {
            if (index != null || params != null) {
                throw new TokenizerError("Unexpected `[` in identifier");
            }
            index = new SetToken(SetToken.BracketType.SQUARE_BRACKETS, location);
            return this;
        } else if (c == '(') {
            if (params != null || index != null) {
                throw new TokenizerError("Unexpected `(` in identifier");
            }
            params = new SetToken(SetToken.BracketType.PARENTHESES, location);
            return this;
        }
        return null;
    }

    @Override
    public String toString() {
        String out = "IdentifierToken{value=\"";
        out += value + "\"";
        if (index != null) {
            out += ", index=" + index.toString();
        } else if (params != null) {
            out += ", params=" + params.toString();
        }
        if (subTokens != null)
            out += ", subTokens=" + subTokens.size();
        out += ", " + startLocation;
        return out + "}";
    }

    @Override
    public String debugString() {
        String out = value;
        if (index != null) {
            out += index.debugString();
        } else if (params != null) {
            out += params.debugString();
        }
        if (subTokens != null) {
            for (Token t : subTokens) {
                out += "." + ((IdentifierToken) t).debugString();
            }
        }
        return out;
    }

    public static boolean validStart(char c) {
        return Character.isAlphabetic(c) || c == '_';
    }

    public Identifier asId() {
        return new Identifier(this);
    }

    public IdentifierToken sub(int i) {
        return (IdentifierToken) subTokens.get(i);
    }
    
    public IdentifierToken next() {
        if (subTokens == null || subTokens.isEmpty())
            return null;
        return (IdentifierToken) subTokens.getFirst();
    }

    public Span spanFirst() {
        return startLocation.span(startLocation.add(value.length() - 1));
    }

    public boolean simple() {
        return params == null && index == null;
    }

    public boolean indexed() {
        return index != null;
    }

    public boolean hasParams() {
        return params != null;
    }

    public boolean hasParamsSub() {
        if (params != null)
            return true;
        if (subTokens == null || subTokens.isEmpty())
            return false;
        return ((IdentifierToken)subTokens.getFirst()).hasParamsSub();
    }

    public SetToken getParamsSub() {
        if (params != null)
            return params;
        if (subTokens == null || subTokens.isEmpty())
            return null;
        return ((IdentifierToken)subTokens.getFirst()).getParamsSub();
    }
    
}
