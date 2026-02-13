package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;

public class StackAllocAction extends Action {

    public int reg = 1;

    public StackAllocAction(ActionScope scope, int reg) {
        super(scope);
        this.reg = reg;
    }

    @Override
    public String toAssembly() {
        if(reg < 0)
            return "STACK INC";
        return String.format("STACK PUSH %s", MachineCode.translateReg(reg));
    }


}
