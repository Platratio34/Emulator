package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.ConditionalOperator;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.SetInstruction;

public class SetKeyword extends ASMKeyword {

    public SetKeyword() {
        super("SET");
    }

    @Override
    public Instruction add(ASMLine line) {
        // SET (FORCE) <EQ|NEQ|LT|LEQ|GT|GEQ> [rg] [rd]

        boolean forced = line.hasNext("FORCE");
        if(forced)
            line.symbolLast(Type.KEYWORD);
        String ineq = line.nextString(AsmError.error("Expected conditional"));
        if(ineq == null)
            return null;
        ConditionalOperator op = switch(ineq) {
            case "EQ" -> ConditionalOperator.EQ_ZERO;
            case "NEQ" -> ConditionalOperator.NEQ_ZERO;
            
            case "LT" -> ConditionalOperator.LT_ZERO;
            case "LEQ" -> ConditionalOperator.LEQ_ZERO;
            
            case "GT" -> ConditionalOperator.GT_ZERO;
            case "GEQ" -> ConditionalOperator.GEQ_ZERO;

            default -> null;
        };
        if (op == null) {
            line.errorLast(AsmError.error("Unknown conditional"));
            return null;
        }
        line.symbolLast(Type.KEYWORD);
        Reg rg = line.nextReg(AsmError.error("Expected rg register"));
        if(rg == null)
            return null;
        Reg rd = line.nextReg(AsmError.error("Expected rd register"));
        if(rd == null)
            return null;
        if(forced)
            return SetInstruction.Forced(op, rg, rd);
        return SetInstruction.NonForced(op, rg, rd);
    }

    @Override
    public String getDef() {
        return "Conditional set operator";
    }

    @Override
    public String getUsage() {
        return "`SET (FORCE) <EQ|NEQ|LT|LEQ|GT|GEQ> [rg] [rd]`";
    }

}
