package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.TempStore;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MemorySize;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.StoreInstruction;

public class StoreKeyword extends ASMKeyword {

    public StoreKeyword() {
        super("STORE");
    }

    @Override
    public Instruction add(ASMLine line) {
        // STORE (<WORD|SHORT|BYTE>) rg ra (INC_RA)
        // STORE (<WORD|SHORT|BYTE>) [const] ra (INC_RA) 
        // STORE (<WORD|SHORT|BYTE>) rg [const] 

        Reg rg = line.nextReg();
        MemorySize size = MemorySize.WORD;
        if (rg == null) { // STORE <WORD|SHORT|BYTE> ...
            if (line.hasNext("WORD")) {
                line.symbolLast(Type.KEYWORD);
                rg = line.nextReg();
            } else if (line.hasNext("SHORT")) {
                size = MemorySize.SHORT;
                line.symbolLast(Type.KEYWORD);
                rg = line.nextReg();
            } else if (line.hasNext("BYTE")) {
                size = MemorySize.BYTE;
                line.symbolLast(Type.KEYWORD);
                rg = line.nextReg();
            }
        }
        if (rg == null) { // STORE (<WORD|SHORT|BYTE>) [const] ra (INC_RA) 
            Define def = line.nextConst(AsmError.error("Expected rg register or constant"));
            if (def == null)
                return null;
            Reg ra = line.nextReg(AsmError.error("Expected ra register"));
            boolean incRA = line.hasNext("INC_RA");
            if (incRA)
                line.symbolLast(Type.KEYWORD);
            return new TempStore(size, def, ra, incRA);
        }
        // STORE (<WORD|SHORT|BYTE>) rg [const] 
        Reg ra = line.nextReg();
        if (ra == null) { // STORE (<WORD|SHORT|BYTE>) rg [const] 
            Define def = line.nextConst(AsmError.error("Expected ra register or constant address"));
            if (def == null)
                return null;
            return new TempStore(size, rg, def);
        }
        // STORE (<WORD|SHORT|BYTE>) rg ra (INC_RA)
        boolean incRA = line.hasNext("INC_RA");
        if (incRA)
            line.symbolLast(Type.KEYWORD);
        return StoreInstruction.StoreReg(size, rg, ra, incRA);
    }
    
    @Override
    public String getDef() {
        return "Store to memory. `mem[ra] = rg` or `mem[ra] = [value]` or `mem[address] = rg`";
    }

    @Override
    public String getUsage() {
        return "`STORE (<WORD|SHORT|BYTE>) <[rg]|[value]> [ra] (INC_RA)` or `STORE (<WORD|SHORT|BYTE>) [rg] [address]`";
    }

}
