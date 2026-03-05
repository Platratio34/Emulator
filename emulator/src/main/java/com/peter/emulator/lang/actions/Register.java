package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;

public class Register {

    protected int reg = -1;
    protected String regStr = null;
    protected boolean reserved = false;

    public final ActionScope scope;

    public Register(ActionScope scope) {
        if(scope == null)
            throw new NullPointerException();
        this.scope = scope;
    }

    public Register(ActionScope scope, int reg) {
        if(scope == null)
            throw new NullPointerException();
        this.scope = scope;
        this.reg = reg;
        regStr = MachineCode.translateReg(reg);
    }

    public boolean fistFree() {
        if(reg != -1)
            return true;
        reg = scope.firstFreeR();
        regStr = MachineCode.translateReg(reg);
        return reg >= 0;
    }

    public void reserve() {
        if (reg == -1 || reg >= 0x10)
            return;
        scope.reserve(reg);
        reserved = true;
    }

    public void release() {
        if (!reserved)
            return;
        scope.release(reg);
        reserved = false;
    }
    
    public boolean isReserved() {
        if (reg == -1 || reg >= 0x10)
            return false;
        return scope.isReserved(reg);
    }

    public String str() {
        return regStr;
    }

    @Override
    public String toString() {
        return regStr != null ? regStr : "[un-set register]";
    }

    public Register next() {
        return scope.makeHandle(reg + 1);
    }

    public static Register of(ActionScope scope, String vN) {
        return new Register(scope, ActionBlock.getSysDReg(vN));
    }
}
