package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;

public class AddKeyword extends ASMKeyword {

    private final boolean sub;

    public AddKeyword(boolean sub) {
        super(sub ? "SUB" : "ADD");
        this.sub = sub;
    }

    @Override
    public Instruction add(ASMLine line) {
        // <ADD|SUB> rd ra <rb|[const]>
        Reg rd = line.nextReg(AsmError.error("Expected rd register"));
        if (rd == null)
            return null;
        
        Reg ra = line.nextReg(AsmError.error("Expected ra register"));
        
        Reg rb = line.nextReg();
        if (rb != null) { // <ADD|SUB> rd ra rb
            return sub ? MathInstruction.Sub(rd, ra, rb) : MathInstruction.Add(rd, ra, rb);
        }

        // <ADD|SUB> rd ra [const]
        
        Define def = line.nextConst(AsmError.error("Expected register or constant"));
        if (def == null) {
            return null;
        }
        if (def.value > 0xff || def.value < 0) {
            line.errorLast(AsmError.error("Value must be between 0 and 255 inclusive for literal add"));
            return null;
        }
        return sub ? MathInstruction.SubLit(rd, ra, def.value) : MathInstruction.AddLit(rd, ra, def.value);
    }

    @Override
    public String getDef() {
        return sub ? "Subtraction. `rd = ra - rb`. If `rb` is a literal, must be between 0 and 255 inclusive." : "Addition. `rd = ra - rb`. If `rb` is a literal, must be between 0 and 255 inclusive.";
    }

    @Override
    public String getUsage() {
        return sub ? "`SUB [rd] [ra] [rb]` or `SUB [rd] [ra] [amount]`" : "`ADD [rd] [ra] [rb]` or `ADD [rd] [ra] [amount]`";
    }

}
