package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;

public class StackAllocAction extends Action {

    public final Register reg;
    public final ELType type;
    public final ELVariable var;

    public StackAllocAction(ActionScope scope, ELVariable var, Register reg) {
        super(scope);
        this.reg = reg;
        this.var = var;
        this.type = var.type;
    }

    @Override
    public String toAssembly() {
        int size = (Math.ceilDiv(type.sizeof(), 4) * 4);
        String sVarStr = String.format("#stackVar %s %s", type.typeString(), var.name);
        if (reg != null) {
            if(size > 4)
                return String.format("%s\nSTACK PUSH %s\nSTACK INC %d", sVarStr, reg, size - 4);
            return String.format("%s\nSTACK PUSH %s", sVarStr, reg);
        }
        return String.format("%s\nSTACK INC %d", sVarStr, size);
    }


}
