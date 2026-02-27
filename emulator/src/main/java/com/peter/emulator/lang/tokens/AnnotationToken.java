package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.Location;

public class AnnotationToken extends Token {
    public  String name;
    public SetToken params = null;

    public AnnotationToken(Location location) {
        super(location);
        name = "";
    }

    @Override
    public Token ingest(char c, Location location) {
        boolean isDigit = Character.isDigit(c);
        if (params != null) {
            SetToken tkn = (SetToken)params.ingest(c, location);
            if (tkn != null) {
                endLocation = location;
                params = tkn;
                return this;
            }
            return null;
        } else if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
            if (name.length() == 0 && isDigit)
                throw new TokenizerError("Annotation name can not start with a digit");
            name += c;
            endLocation = location;
            return this;
        } else if (c == '(') {
            params = new SetToken(SetToken.BracketType.PARENTHESES, location);
            subTokens = params.subTokens;
            endLocation = location;
            return this;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("AnnotationToken{name=\"%s\", %s}", name, startLocation);
    }

    @Override
    public String debugString() {
        String out = "@" + name;
        if (params != null) {
            out += "(" + params.debugString() + ")";
        }
        return out;
    }
}