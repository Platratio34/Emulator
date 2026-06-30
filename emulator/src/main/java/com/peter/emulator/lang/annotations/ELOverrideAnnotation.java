package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.ELCompileException;
import com.peter.emulator.lang.tokens.AnnotationToken;

public class ELOverrideAnnotation extends ELAnnotation {

    public ELOverrideAnnotation(AnnotationToken token) {
        super(token);
        if (!name.equals("Override"))
            throw new ELCompileException("Tried to create Override annotation, but had wrong name");
    }
    
    @Override
    public String getDescription() {
        return "Marks this function as overriding a function in a parent class. If this annotation is present but the function signature **does not** match a function in the parent, a compiler error will be emitted.";
    }

}
