package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Syscall;

public class SysGotoKeyword extends ASMKeyword {

    public SysGotoKeyword() {
        super("SYSGOTO");
    }

    @Override
    public Instruction add(ASMLine line) {
        Reg ra = line.nextReg(AsmError.error("Expected ra register"));
        if (ra == null)
            return null;
        return Syscall.Goto(ra);
    }

    @Override
    public String getDef() {
        return "Goto an address, setting privileged mode to to `false`. **Requires PM**";
    }

    @Override
    public String getUsage() {
        return "`SYSGOTO [ra]`";
    }

}
