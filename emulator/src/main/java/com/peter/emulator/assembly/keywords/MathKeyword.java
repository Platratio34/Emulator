package com.peter.emulator.assembly.keywords;

import java.util.Map;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.MathInstruction.Operation;
import com.peter.emulator.machinecode.Reg;

public class MathKeyword extends ASMKeyword {

    private final Operation operation;

    public static final Map<Operation, String> NAMES = Map.of(
        Operation.DIV, "DIV",
        Operation.MUL, "MUL",
        Operation.AND, "AND",
        Operation.OR, "OR",
        Operation.NOR, "NOR",
        Operation.XOR, "XOR",
        Operation.NAND, "NAND"
    );

    public MathKeyword(Operation operation) {
        super(NAMES.getOrDefault(operation, "MATH"));
        this.operation = operation;
    }

    @Override
    public Instruction add(ASMLine line) {
        // <ADD|SUB> rd ra <rb|[const]>
        Reg rd = line.nextReg(AsmError.error("Expected rd register"));
        if (rd == null)
            return null;

        Reg ra = line.nextReg(AsmError.error("Expected ra register"));

        Reg rb = line.nextReg(AsmError.error("Expected rb register"));
        return new MathInstruction(operation, rd, ra, rb);
    }
    
    @Override
    public String getDef() {
        return switch(operation) {
            case MUL -> "Multiplication. `rd = ra * rb`";
            case DIV -> "Division. `rd = ra / rb`";
            
            case AND -> "Bitwise And. `rd = ra & rb`";
            case NAND -> "Bitwise And. `rd = ~(ra & rb)`";

            case OR -> "Bitwise And. `rd = ra | rb`";
            case XOR -> "Bitwise And. `rd = ra ^ rb`";
            case NOR -> "Bitwise And. `rd = ~(ra | rb)`";

            default -> "Unknown math operation";
        };
    }

    @Override
    public String getUsage() {
        return String.format("`%s rd ra rb`", id);
    }

}
