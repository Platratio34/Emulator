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
        int size = (Math.ceilDiv(type.sizeof(), 4) * 4);
        if (reg != null) {
            if(size > 4)
                return String.format("STACK PUSH %s\nSTACK INC %d", reg, size - 4);
            return String.format("STACK PUSH %s", reg);
        }
        return String.format("STACK INC %d", size);
    }


}
