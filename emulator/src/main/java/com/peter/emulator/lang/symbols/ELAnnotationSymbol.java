package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.annotations.ELAnnotation;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.NumberToken;
import com.peter.emulator.lang.tokens.Token;

public class ELAnnotationSymbol extends ELSymbol {

    public final ELAnnotation annotation;

    public ELAnnotationSymbol(ELAnnotation annotation) {
        super(Type.ANNOTATION, annotation.span());
        this.annotation = annotation;

        if(annotation.tokens != null) {
            for (Token t : annotation.tokens) {
                if (t instanceof IdentifierToken) {
                    addSymbol(Type.KEYWORD, t.span());
                } else if (t instanceof NumberToken) {
                    addSymbol(Type.NUMERIC_LITERAL, t.span());
                }
            }
        }
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        String out = "";
        out += String.format("`@%s`", annotation.name);
        String desc = annotation.getDescription();
        if (desc != null) {
            out += "\n\n" + desc;
        }
        return out;
    }

}