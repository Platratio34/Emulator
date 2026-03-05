package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;

public class StackAllocAction extends Action {

    public final Register reg;
    public final ELType type;

    public StackAllocAction(ActionScope scope, ELVariable var, Register reg) {
        super(scope);
        this.reg = reg;
        this.type = var.type;
    }

    @Override
    public String toAssembly() {
        if (type.sizeofWords() > 1)
            return "STACK INC " + type.sizeofWords();
        if(reg == null)
            return "STACK INC";
        return String.format("STACK PUSH %s", reg);
    }


}
