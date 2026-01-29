package com.peter.emulator.lang.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.AnnotationToken;

public class ELAnnotation {

    public static final HashMap<String, Function<AnnotationToken, ELAnnotation>> types = new HashMap<>();
    static {
        types.put("Override", ELOverrideAnnotation::new);
        types.put("Operator", ELOperatorAnnotation::new);
    }

    public final String name;
    public final Location startLocation;
    public final Location endLocation;
    protected ArrayList<Token> tokens;

    public ELAnnotation(AnnotationToken token) {
        name = token.name;
        startLocation = token.startLocation;
        endLocation = token.endLocation;
        if(token.params != null)
            tokens = token.params.subTokens;
    }

    public static ELAnnotation create(AnnotationToken token) {
        if (types.containsKey(token.name))
            return types.get(token.name).apply(token);
        return new ELAnnotation(token);
    }

    public String debugString() {
        String out = "@" + name;
        if (tokens != null) {
            out += "(";
            boolean f = true;
            for (Token t : tokens) {
                if (!f && t.wsBefore())
                    out += " ";
                f = false;
                out += t.debugString();
            }
            out += ")";
        }
        return out;
    }
}
