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
        if (type.sizeof() >= 4 || reg == null)
            return "STACK INC " + (Math.ceilDiv(type.sizeof(), 4) * 4);
        return String.format("STACK PUSH %s", reg);
    }


}
