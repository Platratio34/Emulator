package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;

public class IncKeyword extends ASMKeyword {

    public IncKeyword() {
        super("INC");
    }

    @Override
    public Instruction add(ASMLine line) {
        // INC rg [const]
        Reg rg = line.nextReg(AsmError.error("Expected register"));
        if (rg == null)
            return null;
        Define def = line.nextConst();
        if (def == null) {
            return MathInstruction.Inc(rg, 1);
        }
        if (!MathInstruction.inIncRange(def.value)) {
            line.errorLast(
                    AsmError.error("Inc amount must be in the range -32,766 to 32,766 excluding 0, was %d", def.value));
            return null;
        }
        return MathInstruction.Inc(rg, def.value);
    }

    @Override
    public String getDef() {
        return "Increment (or decrement) register. Defaults to an amount of `1`. Amount must be in the range -32,766 to 32,766 excluding 0";
    }

    @Override
    public String getUsage() {
        return "INC [rg] ([amount])";
    }
}
