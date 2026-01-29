package com.peter.emulator.lang;

import java.util.ArrayList;

import com.peter.emulator.lang.Token.NumberToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.StringToken;
import com.peter.emulator.lang.annotations.ELAnnotation;

public class ELVariable {

    public int offset = -1;
    public final String name;
    public final ELType type;
    public final ELProtectionLevel protection;
    public final boolean stat;
    public final boolean finalVal;
    public final Location startLocation;

    public ArrayList<Token> valueTokens = null;
    public ELValue startingValue = null;
    public Location valueLocation = null;
    public ArrayList<ELAnnotation> annotations = null;

    public ELVariable(ELProtectionLevel protection, boolean stat, ELType type, String name, boolean finalVal, Location location) {
        this.type = type;
        this.protection = protection;
        this.stat = stat;
        this.name = name;
        this.finalVal = finalVal;
        this.startLocation = location;
    }

    public int sizeof() {
        return 4;
    }

    public String typeString() {
        return type.typeString();
    }

    public boolean typeEquals(ELVariable other) {
        return type.equals(other.type);
    }

    public boolean hasValue() {
        return valueTokens != null;
    }

    public String getValueDebug() {
        String out = "";
        boolean f = true;
        for (Token token : valueTokens) {
            if (!f)
                out += " ";
            f = false;
            out += token.debugString();
        }
        return out;
    }

    public String debugString() {
        String out = "";
        out += protection.value + " ";
        if (stat)
            out += "static ";
        if (finalVal)
            out += "final ";
        out += type.typeString() + " ";
        out += name;
        if(startingValue != null)
            out += " = " + startingValue.valueString();
        else if (valueTokens != null)
            out += " = " + getValueDebug();
        return out;
    }

    public boolean ingestValue(Token token) {
        if (token instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON) {
            if (valueTokens.size() == 1) {
                if(valueTokens.get(0) instanceof NumberToken nt)
                    startingValue = ELValue.number(type, nt);
                else if(valueTokens.get(0) instanceof StringToken st)
                    startingValue = ELValue.string(type, st);
            }
            return false;
        }
        if (valueTokens == null) {
            valueTokens = new ArrayList<>();
        }
        valueTokens.add(token);
        return true;
    }

    public void analyze(ArrayList<ELAnalysisError> errors, Namespace namespace, ProgramModule module) {
        type.analyze(errors, namespace, module);
    }

    public int getAddress() {
        return offset;
    }

    public ELVariable setValue(int i) {
        startingValue = new ELValue.ELNumberValue(type, i);
        return this;
    }
}
