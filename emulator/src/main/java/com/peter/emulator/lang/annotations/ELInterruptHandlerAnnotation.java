package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.tokens.Token;
import com.peter.emulator.lang.tokens.AnnotationToken;
import com.peter.emulator.lang.tokens.IdentifierToken;

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

    @Override
    public String getDescription() {
        String out = "Marks this function as the interrupt handler function. If not marked `raw`, this function will be executed **after** language level interrupts are handled.";
        out += "\n\nRaw: `" + (raw ? "true": "false") + "`";
        return out;
    }

}
