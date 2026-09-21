package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;

public class NotKeyword extends ASMKeyword {

    public NotKeyword() {
        super("NOT");
    }

    @Override
    public Instruction add(ASMLine line) {
        Reg rd = line.nextReg(AsmError.error("Expected rd register"));
        if(rd == null)
            return null;
        Reg ra = line.nextReg(AsmError.error("Expected ra register"));
        if(ra == null)
            return null;
        return MathInstruction.Not(rd, ra);
    }

    @Override
    public String getDef() {
        return "Bitwise not";
    }

    @Override
    public String getUsage() {
        return "`NOT [rd] [ra]`";
    }

}
