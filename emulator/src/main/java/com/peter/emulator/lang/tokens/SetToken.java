package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.Location;

public class SetToken extends Token {
    public boolean closed = false;
    protected Tokenizer tk;

    protected BracketType type;

    public SetToken(BracketType type, Location location) {
        super(location);
        this.type = type;
        tk = new Tokenizer("", location);
        subTokens = tk.tokens;
    }

    @Override
    public SetToken ingest(char c, Location location) {
        if (closed)
            return null;
        if (tk.ingest(c, location)) {
            endLocation = location;
            return this;
        }
        if (c == type.close) {
            closed = true;
            endLocation = location;
            return this;
        }
        throw new TokenizerError("Found unexpected character in set: '" + c + "'");
    }

    public Token get(int i) {
        if(i > subTokens.size())
            return null;
        return subTokens.get(i);
    }

    @Override
    public String toString() {
        return String.format("SetToken{closed=%s, numTokens=%d, type=%s %s}", closed ? "true" : "false",
                subTokens == null ? 0 : subTokens.size(), type, startLocation);
    }

    @Override
    public String debugString() {
        String out = ""+type.open;
        for (int i = 0; i < subTokens.size(); i++) {
            if (i > 0 && subTokens.get(i).wsBefore())
                out += " ";
            out += subTokens.get(i).debugString();
        }
        return out + type.close;
    }

    public enum BracketType {
        PARENTHESES('(',')'),
        SQUARE_BRACKETS('[',']'),
        ;
        public final char open;
        public final char close;
        private BracketType(char open, char close) {
            this.open = open;
            this.close = close;
        }
    }
}