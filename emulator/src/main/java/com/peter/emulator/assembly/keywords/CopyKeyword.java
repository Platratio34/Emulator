package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Load;
import com.peter.emulator.machinecode.MemorySize;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.StoreInstruction;

public class CopyKeyword extends ASMKeyword {

    public CopyKeyword() {
        super("COPY");
    }

    @Override
    public Instruction add(ASMLine line) {
        // COPY rs rd
        // COPY MEM (<WORD|SHORT|BYTE>) rs rd (INC_RS) (INC_RD)

        if (line.hasNext("MEM")) { // COPY MEM (<WORD|SHORT|BYTE>) rs rd (INC_RS) (INC_RD)
            line.symbolLast(Type.KEYWORD);
            Reg rs = line.nextReg();
            MemorySize size = MemorySize.WORD;
            if (rs == null) { // COPY MEM <WORD|SHORT|BYTE> rs rd (INC_RS) (INC_RD)
                String str = line.nextString(AsmError.error("Expected rs register or memory size"));
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
                rs = line.nextReg(AsmError.error("Expected rs register"));
                if (rs == null)
                    return null;
            }
            Reg ra = line.nextReg(AsmError.error("Expected rd register"));
            if (ra == null) {
                return null;
            }
            // COPY MEM (<WORD|SHORT|BYTE>) rs rd (INC_RS) (INC_RD)
            StoreInstruction instr = StoreInstruction.CopyMem(size, rs, ra);
            boolean incRS = line.hasNext("INC_RS");
            if (incRS) {
                line.symbolLast(Type.KEYWORD);
                instr.withIncRG();
            }
            boolean incRD = line.hasNext("INC_RD");
            if (incRD) {
                line.symbolLast(Type.KEYWORD);
                instr.withIncRA();
            }
            return instr;
        } else { // LOAD COPY rs rd
            Reg rs = line.nextReg(AsmError.error("Expected rs register"));
            if (rs == null)
                return null;
            Reg rd = line.nextReg(AsmError.error("Expected rd register"));
            if (rd == null)
                return null;
            return Load.Copy(rs, rd);
        }
    }
    
    @Override
    public String getDef() {
        return "Copy register or memory";
    }

    @Override
    public String getUsage() {
        return "`COPY rs rd` or `COPY MEM (<WORD|SHORT|BYTE>) rs rd (INC_RS) (INC_RD)`";
    }

}
