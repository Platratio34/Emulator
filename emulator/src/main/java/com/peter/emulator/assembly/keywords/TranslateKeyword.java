package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Syscall;

public class TranslateKeyword extends ASMKeyword {

    public TranslateKeyword() {
        super("TRANSLATE");
    }

    @Override
    public Instruction add(ASMLine line) {
        Reg rg = line.nextReg(AsmError.error("Expected register"));
        if(rg == null)
            return null;
        return Syscall.Translate(rg);
    }

    @Override
    public String getDef() {
        return "Translate user address to physical address. **Requires PM**";
    }

    @Override
    public String getUsage() {
        return "`TRANSLATE [rg]`";
    }

}
