package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.tokens.AnnotationToken;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class ELBreakpointAnnotation extends ELAnnotation {

    public boolean noOp = false;

    public ELBreakpointAnnotation(AnnotationToken token) {
        super(token);
        if(!token.subTokens.isEmpty()) {
            if(token.subTokens.get(0) instanceof IdentifierToken it) {
                noOp = it.value.equals("noOp");
            }
        }
    }

    @Override
    public void analyze(ProgramUnit unit) {
        throw ELAnalysisError.error("Breakpoint annotation not allowed outside code block",
                startLocation.span(endLocation));
    }
    
    @Override
    public String getDescription() {
        String out = "Inserts a breakpoint at this location. If marked `noOp`, a NO OP instruction will also be inserted to ensure the breakpoint is hit.";
        out += "\n\nNoOp: `" + (noOp ? "true": "false") + "`";
        return out;
    }

}
