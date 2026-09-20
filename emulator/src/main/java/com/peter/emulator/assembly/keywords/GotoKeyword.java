package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.assembly.Define;
import com.peter.emulator.assembly.TempGoto;
import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.machinecode.ConditionalOperator;
import com.peter.emulator.machinecode.Goto;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Goto.Mode;

public class GotoKeyword extends ASMKeyword {

    public GotoKeyword() {
        super("GOTO");
    }

    @Override
    public Instruction add(ASMLine line) {
        // GOTO (PUSH) (<EQ|LEQ|GT|NEQ|LT|GEQ> rg) <[:label]|ra>
        // GOTO POP (<EQ|LEQ|GT|NEQ|LT|GEQ> rg)
        if (line.hasNext("POP")) { // GOTO POP (<EQ|LEQ|GT|NEQ|LT|GEQ> rg)
            line.symbolLast(Type.KEYWORD);
            String ineq = line.nextString();
            if (ineq == null) {
                return Goto.Pop(ConditionalOperator.UNCONDITIONAL, Reg.R0);
            }
            ConditionalOperator op = switch (ineq) {
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
            return Goto.Pop(op, rg);
        }

        // GOTO (PUSH) (<EQ|LEQ|GT|NEQ|LT|GEQ> rg) <[:label]|ra>

        boolean push = line.hasNext("PUSH");

        ConditionalOperator op = ConditionalOperator.UNCONDITIONAL;
        Reg rg = Reg.R0;
        if (line.hasNext("EQ")) {
            op = ConditionalOperator.EQ_ZERO;
        } else if (line.hasNext("NEQ")) {
            op = ConditionalOperator.NEQ_ZERO;
        } else if (line.hasNext("LT")) {
            op = ConditionalOperator.LT_ZERO;
        } else if (line.hasNext("LEQ")) {
            op = ConditionalOperator.LEQ_ZERO;
        } else if (line.hasNext("GT")) {
            op = ConditionalOperator.GT_ZERO;
        } else if (line.hasNext("GEQ")) {
            op = ConditionalOperator.GEQ_ZERO;
        }
        if (op != ConditionalOperator.UNCONDITIONAL) {
            line.symbolLast(Type.KEYWORD);
            rg = line.nextReg(AsmError.error("Expected rg register"));
        }

        Reg ra = line.nextReg();
        if (ra == null) {
            String name = line.nextString(AsmError.error("Expected ra register or label"));
            if (name == null)
                return null;
            if (!name.startsWith(":")) {
                line.errorLast(AsmError.error("Expected ra register or label"));
                return null;
            }
            Define lbl = line.getLabel(name.substring(1));
            if (lbl == null) {
                line.errorLast(AsmError.error("Unknown label"));
                return null;
            }
            line.symbolLast(Type.FUNCTION_NAME);
            return new TempGoto(op, push ? Mode.PUSH : Mode.NONE, rg, lbl);
        }
        return new Goto(op, push ? Mode.PUSH : Mode.NONE, ra, rg);
    }

    @Override
    public String getDef() {
        return "Conditional Goto.";
    }

    @Override
    public String getUsage() {
        return "`GOTO (PUSH) (<EQ|NEQ|LT|LEQ|GT|GEQ> [rg]) <[:label]|[ra]>` or `GOTO POP (<EQ|NEQ|LT|LEQ|GT|GEQ> [rg])`";
    }
}
