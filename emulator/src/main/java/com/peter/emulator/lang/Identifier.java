package com.peter.emulator.lang;

import com.peter.emulator.lang.Token.IdentifierToken;

public class Identifier {

    public final String fullName;
    public final String[] parts;

    public Identifier(IdentifierToken it) {
        String n = it.value;
        if(it.subTokens != null) {
            parts = new String[it.subTokens.size()+1];
            parts[0] = it.value;
            for(int i = 0; i < it.subTokens.size(); i++) {
                String s = ((IdentifierToken)it.subTokens.get(i)).value;
                n += "." + s;
                parts[i+1] = s;
            }
        } else {
            parts = new String[] {n};
        }
        fullName = n;
    }

    @Override
    public String toString() {
        return String.format("Identifier{%s}", fullName);
    }

    @Override

    public boolean equals(Object obj) {
        if(obj instanceof String s)
            return s.equals(fullName);
        if(obj instanceof IdentifierToken it)
            return new Identifier(it).fullName.equals(fullName);
        if(obj instanceof Identifier id)
            return id.fullName.equals(fullName);
        return false;
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    public String last() {
        return parts[parts.length-1];
    }

    public String first() {
        return parts[0];
    }

    public String get(int i) {
        return parts[i];
    }
}
