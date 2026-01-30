package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;

public class StackAllocAction extends Action {

    public int reg = 1;

    public StackAllocAction(int reg) {
        this.reg = reg;
    }

    @Override
    public String toAssembly() {
        if(reg < 0)
            return "INC rStack";
        return String.format("STACK PUSH %s", MachineCode.translateReg(reg));
    }


}
