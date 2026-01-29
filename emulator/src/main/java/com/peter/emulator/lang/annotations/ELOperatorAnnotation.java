package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.ELCompileException;
import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.AnnotationToken;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.OperatorToken;

public class ELOperatorAnnotation extends ELAnnotation {

    public final OperatorToken.Type type;
    public final boolean cast;

    public ELOperatorAnnotation(AnnotationToken token) {
        super(token);
        if(!name.equals("Operator"))
            throw new ELCompileException("Tried to create Operator annotation, but had wrong name");
        Token tkn = token.params.get(0);
        if (tkn instanceof IdentifierToken idt) {
            if (idt.value.equals("cast")) {
                cast = true;
            } else {
                cast = false;
            }
        } else {
            cast = false;
        }
        if (tkn instanceof OperatorToken ot) {
            type = ot.type;
        } else {
            type = null;
        }
        if (cast == false && type == null) {
            throw new ELCompileException("Missing operator type in @Operator annotation");
        }
    }

}
