package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.TempTestAndSet;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.TestAndSet;

public class TestNSetKeyword extends ASMKeyword {

    public TestNSetKeyword() {
        super("TEST");
    }

    @Override
    public Instruction add(ASMLine line) {
        // TEST AND SET [rg] <[ra]|[address]>
        if (!line.hasNext("AND", AsmError.error("Unknown keyword")))
            return null;
        line.symbolLast(Type.KEYWORD);
        if (!line.hasNext("SET", AsmError.error("Unknown keyword")))
            return null;
        line.symbolLast(Type.KEYWORD);
        Reg rg = line.nextReg(AsmError.error("Expected rg register"));
        if(rg == null)
            return null;
        Reg ra = line.nextReg();
        if (ra != null) {
            return new TestAndSet(rg, ra);
        }
        Define def = line.nextConst(AsmError.error("Expected ra register or constant address"));
        if(def == null)
            return null;
        return new TempTestAndSet(rg, def);
    }

    @Override
    public String getDef() {
        return "Atomic test and set. Sets rg to memory at address, and sets the address to `1`";
    }

    @Override
    public String getUsage() {
        return "`TEST AND SET [rg] <[ra]|[address]>`";
    }

}
