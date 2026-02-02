package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.Arrays;

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

    public Identifier(ArrayList<String> pList) {
        String n = pList.get(0);
        parts = new String[pList.size()];
        for(int i = 0; i < pList.size(); i++) {
            String s = pList.get(i);
            n += "." + s;
            parts[i+1] = s;
        }
        fullName = n;
    }

    public Identifier(String name) {
        parts = new String[] { name };
        fullName = name;
    }

    public Identifier(String[] pArr) {
        String n = pArr[0];
        parts = new String[pArr.length];
        for(int i = 0; i < pArr.length; i++) {
            String s = pArr[i];
            n += "." + s;
            parts[i+1] = s;
        }
        fullName = n;
    }

    @Override
    public String toString() {
        return fullName;
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
    public boolean equals(String str) {
        return str.equals(fullName);
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

    public boolean starts(String part) {
        return parts[0].equals(part);
    }

    public boolean starts(String... parts) {
        for (int i = 0; i < parts.length; i++) {
            if (i >= this.parts.length)
                return false;
            if (!parts[i].equals(this.parts[i]))
                return false;
        }
        return true;
    }

    public boolean partEquals(int i, String part) {
        if (parts.length <= i)
            return false;
        return parts[i].equals(part);
    }

    public Builder builder() {
        return new Builder().ingest(parts);
    }
    
    public static class Builder {
        protected ArrayList<String> parts = new ArrayList<>();

        public Builder ingest(String part) {
            parts.add(part);
            return this;
        }

        public Builder ingest(String[] arr) {
            parts.addAll(Arrays.asList(arr));
            return this;
        }

        public Identifier build() {
            return new Identifier(parts);
        }
    }

    public int numParts() {
        return parts.length;
    }
}
