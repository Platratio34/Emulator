package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;

public class StackAllocAction extends Action {

    public int reg = 1;
    public ELType type = null;

    public StackAllocAction(ActionScope scope, ELVariable var, int reg) {
        super(scope);
        this.reg = reg;
        this.type = var.type;
    }

    @Override
    public String toAssembly() {
        if (type.sizeofWords() > 1)
            return "STACK INC " + type.sizeofWords();
        if(reg < 0)
            return "STACK INC";
        return String.format("STACK PUSH %s", MachineCode.translateReg(reg));
    }


}
