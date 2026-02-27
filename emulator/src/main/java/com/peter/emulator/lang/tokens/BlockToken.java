package com.peter.emulator.lang.tokens;

import com.peter.emulator.lang.Location;

public class BlockToken extends Token {
    public boolean closed = false;
    protected Tokenizer tk;

    public BlockToken(Location location) {
        super(location);
        tk = new Tokenizer("", location);
        subTokens = tk.tokens;
    }

    @Override
    public Token ingest(char c, Location location) {
        if (closed)
            return null;
        if (tk.ingest(c, location)) {
            endLocation = location;
            return this;
        }
        if (c == '}') {
            endLocation = location;
            closed = true;
            return this;
        }
        throw new TokenizerError("Found unexpected character in block: '" + c + "'");
    }

    @Override
    public String toString() {
        return String.format("BlockToken{closed=%s, numTokens=%d, %s}", closed ? "true" : "false", subTokens == null ? 0 : subTokens.size(), startLocation);
    }

    @Override
    public String debugString() {
        String out = "{";
        for (int i = 0; i < subTokens.size(); i++) {
            if (i > 0 && subTokens.get(i).wsBefore())
                out += " ";
            out += subTokens.get(i).debugString();
        }
        return out + "}";
    }
}