package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.tokens.AnnotationToken;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class ELEntrypointAnnotation extends ELAnnotation {

    public boolean raw;

    public ELEntrypointAnnotation(AnnotationToken token) {
        super(token);
        if(!token.subTokens.isEmpty()) {
            if(token.subTokens.get(0) instanceof IdentifierToken it) {
                raw = it.value.equals("raw");
            }
        }
    }

    @Override
    public String getDescription() {
        String out = "The entrypoint function of the module. If not marked `raw`, this function will be executed **after** language setup is done.";
        out += "\n\nRaw: `" + (raw ? "true": "false") + "`";
        return out;
    }
}
