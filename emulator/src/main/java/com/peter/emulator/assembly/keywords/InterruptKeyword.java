package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Syscall;

public class InterruptKeyword extends ASMKeyword {

    public InterruptKeyword() {
        super("INTERRUPT");
    }

    @Override
    public Instruction add(ASMLine line) {
        if (line.hasNext("RET")) {
            line.symbolLast(Type.KEYWORD);
            return Syscall.InterruptReturn();
        }
        Reg rg = line.nextReg();
        if (rg != null) {
            return Syscall.Interrupt(rg);
        }
        Define def = line.nextConst(AsmError.error("Expected register or constant code"));
        if(def == null)
            return null;
        return Syscall.Interrupt(def.value);
    }

    @Override
    public String getDef() {
        return "Trigger or return from interrupt";
    }

    @Override
    public String getUsage() {
        return "`INTERRUPT RET` or `INTERRUPT <[rg]|[code]>`";
    }

}
