package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;

public class DirectAction extends Action {

    public int reg;
    public String asm;

    public DirectAction(String asm) {
        this.asm = asm;
    }

    public DirectAction(String asm, Object... args) {
        this.asm = String.format(asm, args);
    }

    @Override
    public String toAssembly() {
        return String.format(asm, MachineCode.translateReg(reg));
    }

}
