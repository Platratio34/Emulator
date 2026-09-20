package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MemorySize;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.StackInstruction;

public class StackKeyword extends ASMKeyword {

    public StackKeyword() {
        super("STACK");
    }

    @Override
    public Instruction add(ASMLine line) {
        // STACK <PUSH|POP> (<WORD|SHORT|BYTE>) [rg]
        // STACK INC [amount]
        String op = line.nextString(AsmError.error("Expected `PUSH`, `POP`, `INC`, or `DEC`"));
        if (op == null)
            return null;
        switch (op) {
            case "INC", "DEC" -> {
                line.symbolLast(Type.KEYWORD);
                Define def = line.nextConst(AsmError.error("Expected constant amount"));
                if (def == null)
                    return null;
                if (def.value < 1 || def.value > 0xffff) {
                    line.errorLast(AsmError.error("Amount must be between 1 and 65,536 inclusive"));
                    return null;
                }
                if (op.equals("INC")) {
                    return StackInstruction.Inc(def.value);
                } else {
                    return StackInstruction.Dec(def.value);
                }
            }
            case "PUSH", "POP" -> {
                line.symbolLast(Type.KEYWORD);
                MemorySize size = MemorySize.WORD;
                if(line.hasNext("WORD")) {
                    line.symbolLast(Type.KEYWORD);
                } else if(line.hasNext("SHORT")) {
                    line.symbolLast(Type.KEYWORD);
                    size = MemorySize.SHORT;
                } else if(line.hasNext("BYTE")) {
                    line.symbolLast(Type.KEYWORD);
                    size = MemorySize.BYTE;
                }
                Reg rg = line.nextReg(AsmError.error("Expected register"));
                if (op.equals("PUSH")) {
                    return StackInstruction.Push(size, rg);
                } else {
                    return StackInstruction.Pop(size, rg);
                }
            }
            default -> {
                line.errorLast(AsmError.error("Expected `PUSH`, `POP`, `INC`, or `DEC`"));
                return null;
            }
        }
    }

    @Override
    public String getDef() {
        return "Stack push/pop and increment instructions.";
    }

    @Override
    public String getUsage() {
        return "`STACK <PUSH|POP> [rg]` or `STACK <INC|DEC> [amount]`";
    }

}
