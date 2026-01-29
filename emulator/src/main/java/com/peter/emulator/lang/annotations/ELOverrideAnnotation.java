package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.ELCompileException;
import com.peter.emulator.lang.Token.AnnotationToken;

public class ELOverrideAnnotation extends ELAnnotation {

    public ELOverrideAnnotation(AnnotationToken token) {
        super(token);
        if(!name.equals("Override"))
            throw new ELCompileException("Tried to create Override annotation, but had wrong name");
    }

}
