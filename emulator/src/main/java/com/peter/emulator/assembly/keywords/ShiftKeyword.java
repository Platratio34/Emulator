package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;

public class ShiftKeyword extends ASMKeyword {

    public final boolean right;
    public final boolean rotate;

    public ShiftKeyword(boolean right, boolean rotate) {
        super((right ? "R" : "L") + (rotate ? "RT" : "SH"));
        this.right = right;
        this.rotate = rotate;
    }

    @Override
    public Instruction add(ASMLine line) {
        Reg rd = line.nextReg(AsmError.error("Expected rd register"));
        if (rd == null)
            return null;
        Reg ra = line.nextReg(AsmError.error("Expected ra register"));
        if (ra == null)
            return null;
        Define def = line.nextConst(AsmError.error("Expected constant amount"));
        if(def == null)
            return null;
        
        if (def.value < 1 || def.value > 0x7f) {
            line.errorLast(AsmError.error("Shift amount must between 1 and 127 inclusive"));
            return null;
        }
        if (right) {
            return rotate ? MathInstruction.RRotate(rd, ra, def.value) : MathInstruction.RShift(rd, ra, def.value);
        } else {
            return rotate ? MathInstruction.LRotate(rd, ra, def.value) : MathInstruction.LShift(rd, ra, def.value);
        }
    }

    @Override
    public String getDef() {
        return String.format("%s bitwise %s", right ? "Right" : "Left", rotate ? "rotate" : "shift");
    }

    @Override
    public String getUsage() {
        return String.format("`%s [rd] [ra] [amount]`", id);
    }

}
