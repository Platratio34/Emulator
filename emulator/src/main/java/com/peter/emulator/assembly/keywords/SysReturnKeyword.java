package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Syscall;

public class SysReturnKeyword extends ASMKeyword {

    public SysReturnKeyword() {
        super("SYSRETURN");
    }

    @Override
    public Instruction add(ASMLine line) {
        return Syscall.Return();
    }

    @Override
    public String getDef() {
        return "Return from Syscall. **Requires PM**";
    }

    @Override
    public String getUsage() {
        return "`SYSRETURN`";
    }

}
