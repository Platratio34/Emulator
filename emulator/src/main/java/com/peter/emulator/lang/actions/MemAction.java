package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;

public class MemAction extends Action {
    public boolean load;
    public int rg;
    public int ra;

    /*
    
    var1.mem1 = var2.mem2;
    
    ->
    
    RESOLVE r1 var2 mem2 BY VALUE
    RESOLVE r2 var1 mem1
    MEM STORE r1 r2
    
    ----
    
    var1 = var2 + 1;
    
    ->
    EXPRESSION r1 var2 + 1
    RESOLVE r2 var1
    MEM STORE r1 r2
    
    */


    @Override
    public String toAssembly() {
        String out = "";
        String rgN = MachineCode.translateReg(rg);
        String raN = MachineCode.translateReg(ra);
        if (load) {
            out += String.format("LOAD MEM %s %s\n", rgN, raN);
        } else {
            out += String.format("STORE %s %s\n", rgN, raN);
        }
        return out;
    }
}
