package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.TempLoad;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Load;
import com.peter.emulator.machinecode.MemorySize;
import com.peter.emulator.machinecode.Reg;

public class LoadKeyword extends ASMKeyword {

    public LoadKeyword() {
        super("LOAD");
    }

    @Override
    public Instruction add(ASMLine line) {
        if (line.hasNext("MEM")) { // LOAD MEM (<WORD|SHORT|BYTE>) rg <ra|[const]> (INC_RA)
            line.symbolLast(Type.KEYWORD);
            Reg rg = line.nextReg();
            MemorySize size = MemorySize.WORD;
            if (rg == null) { // LOAD MEM <WORD|SHORT|BYTE> rg <ra|[const]> (INC_RA)
                String str = line.nextString(AsmError.error("Expected rg register or memory size"));
                size = switch (str) {
                    case "WORD" -> MemorySize.WORD;
                    case "SHORT" -> MemorySize.SHORT;
                    case "BYTE" -> MemorySize.BYTE;

                    default -> null;
                };
                if (size == null) {
                    line.errorLast(AsmError.error("Unknown memory size `%s`", str));
                    return null;
                }
                line.symbolLast(Type.KEYWORD);
                rg = line.nextReg(AsmError.error("Expected rg register"));
                if (rg == null)
                    return null;
            }
            Reg ra = line.nextReg();
            if (ra == null) { // LOAD MEM (<WORD|SHORT|BYTE>) rg [const]
                Define def = line.nextConst(AsmError.error("Expected ra register or constant address"));
                if (def == null)
                    return null;
                return new TempLoad(size, rg, def);
            }
            // LOAD MEM (<WORD|SHORT|BYTE>) rg ra (INC_RA)
            boolean incRA = line.hasNext("INC_RA");
            if (incRA)
                line.symbolLast(Type.KEYWORD);
            return Load.Mem(size, rg, ra, incRA);
        } else { // LOAD rg [const]
            Reg rg = line.nextReg(AsmError.error("Expected rg register"));
            if (rg == null)
                return null;
            Define def = line.nextConst(AsmError.error("Expected constant value"));
            if (def == null)
                return null;
            return new TempLoad(rg, def);
        }
    }
    
    @Override
    public String getDef() {
        return "Load into register.";
    }

    @Override
    public String getUsage() {
        return "`LOAD [rg] [value]` or `LOAD MEM (<WORD|SHORT|BYTE>) [rg] [ra] (INC_RA)` or `LOAD MEM (<WORD|SHORT|BYTE>) [rg] [address]`";
    }

}
