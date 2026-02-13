package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.Arrays;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELVariable;

public class ResolveAction extends Action {

    public int reg;
    public ArrayList<ELVariable> vars = new ArrayList<>();
    public boolean resolveValue = false;

    public ResolveAction(ActionScope scope, int reg, ELVariable... vars) {
        super(scope);
        this.reg = reg;
        this.vars.addAll(Arrays.asList(vars));
    }

    public ResolveAction(ActionScope scope, int reg, ArrayList<ELVariable> vars) {
        super(scope);
        this.reg = reg;
        this.vars.addAll(vars);
    }

    @Override
    public String toAssembly() {
        String out ;
        String r = MachineCode.translateReg(reg);
        ELVariable var = vars.get(0);
        if (var.varType == ELVariable.Type.STATIC) {
            out = String.format("LOAD %s &%s",r, var.getQualifiedName());
        } else {
            out = "COPY r15 "+r;
        }
        for (int i = 1; i < vars.size(); i++) {
            if (var.type.isPointer())
                out += String.format("\nLOAD MEM %s %s", r, r);
            var = vars.get(i);
            out += String.format("\nINC %s &%s", r, var.getQualifiedName());
        }
        if (resolveValue) {
            out += String.format("\nLOAD MEM %s %s", r, r);
        }
        return out;
    }

    public ResolveAction byVal() {
        resolveValue = true;
        return this;
    }

    public ResolveAction byVal(boolean byVal) {
        resolveValue = byVal;
        return this;
    }
}
