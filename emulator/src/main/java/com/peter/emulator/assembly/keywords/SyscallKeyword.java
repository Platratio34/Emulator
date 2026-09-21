package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Syscall;

public class SyscallKeyword extends ASMKeyword {

    public SyscallKeyword() {
        super("SYSCALL");
    }

    @Override
    public Instruction add(ASMLine line) {
        // SYSCALL [function]
        int index = -1;

        Define funcDef = line.nextConst();
        if (funcDef != null) {
            if(funcDef.isAddress || funcDef.isLabel) {
                line.errorLast(AsmError.error("Function must be name or constant index"));
                return null;
            }
            index = funcDef.value;
        } else {
            String funcName = line.nextString(AsmError.error("Expected function"));
            index = 0;
            line.errorLast(AsmError.warning("Linked syscalls not currently supported"));
            // TODO linker based syscalls

            line.symbolLast(Type.FUNCTION_NAME);
        }
        return Syscall.Function(index);
    }

    @Override
    public String getDef() {
        return "Syscall instruction.";
    }

    @Override
    public String getUsage() {
        return "`SYSCALL <[name]|[index]>`";
    }

}
