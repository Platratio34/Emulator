package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.AnnotationToken;
import com.peter.emulator.lang.Token.IdentifierToken;

public class ELInterruptHandlerAnnotation extends ELAnnotation {

    public final boolean raw;

    public ELInterruptHandlerAnnotation(AnnotationToken token) {
        super(token);
        boolean r = false;
        if (token.subTokens != null) {
            for (Token t : token.subTokens) {
                if (t instanceof IdentifierToken it && it.value.equals("raw")) {
                    r = true;
                    break;
                } 
            }
        }
        raw = r;
    }

}
