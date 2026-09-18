package com.peter.emulator.lang.annotations;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.tokens.AnnotationToken;
import com.peter.emulator.lang.tokens.NumberToken;

public class ELSyscallAnnotation extends ELAnnotation {

    public int index = 0;

    public ELSyscallAnnotation(AnnotationToken token) {
        super(token);
        if(!token.subTokens.isEmpty()) {
            if (token.subTokens.get(0) instanceof NumberToken nt) {
                index = nt.numValue;
            }
        }
    }

    @Override
    public void analyze(ProgramUnit unit) {
        super.analyze(unit);
        if (index <= 0 || index > 0xff) {
            unit.errors.add(ELAnalysisError.error("Invalid or missing index, must be between 1 and 255 inclusive. Was "+index,
                    startLocation.span(endLocation)));
        }
    }
    
    @Override
    public String getDescription() {
        String out = "Marks a function as a system call. A syscall wrapper will be built and referenced in the syscall table.";
        out += "\n\nIndex: `" + index + "`";
        return out;
    }

    @Override
    public String getDefDesc() {
        return "Syscall: `"+index+"`";
    }

}
