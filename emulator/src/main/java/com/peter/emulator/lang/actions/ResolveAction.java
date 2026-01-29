package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELVariable;

public class ResolveAction extends Action {

    public int reg;
    public ArrayList<ELVariable> vars = new ArrayList<>();
    public boolean resolveValue;

    @Override
    public String toAssembly() {
        String out = "";
        String r = MachineCode.translateReg(reg);
        ELVariable var = vars.get(0);
        if (var.stat) {
            out = String.format("LOAD %s %d",r, var.getAddress());
        } else {
            out = "COPY r15 "+r;
        }
        for (int i = 1; i < vars.size(); i++) {
            if (var.type.isPointer())
                out += String.format("\nLOAD MEM %s %s", r, r);
            var = vars.get(i);
            out += String.format("\nINC %s %d", r, var.getAddress());
        }
        if (resolveValue) {
            out += String.format("LOAD MEM %s %s\n", r, r);
        }
        return out;
    }
}
