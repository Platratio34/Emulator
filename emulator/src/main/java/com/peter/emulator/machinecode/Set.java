package com.peter.emulator.machinecode;

public class Set extends Instruction {

    public final ConditionalOperator condition;
    public final Reg rg;
    public final Reg rd;
    public final boolean forced;

    public static int FORCE_MASK = 0x0010_0000;

    public Set(ConditionalOperator condition, Reg rg, Reg rd, boolean forced) {
        super(Operator.SET);
        this.condition = condition;
        this.rg = rg;
        this.rd = rd;
        this.forced = forced;
    }
    

    public static Set NonForced(ConditionalOperator condition, Reg rg, Reg rd) {
        return new Set(condition, rg, rd, false);
    }
    public static Set Forced(ConditionalOperator condition, Reg rg, Reg rd) {
        return new Set(condition, rg, rd, true);
    }

    public static Set fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.SET.id) {
            return null;
        }
        return new Set(ConditionalOperator.fromBytecode(bytecode), Reg.from(bytecode >> 8), Reg.from(bytecode), (bytecode & FORCE_MASK) != 0);
    }

    @Override
    public int getBytecode() {
        return op.id | (forced ? FORCE_MASK : 0) | condition.id | (rg.code << 8) | rd.code;
    }

    @Override
    public String toString() {
        String out = String.format("SET%s ", forced ? " FORCE" : "");
        return out + switch (condition) {
            case UNCONDITIONAL -> rg.string;
            case EQ_ZERO -> String.format("EQ %s %s", rd.string, rg.string);
            case NEQ_ZERO -> String.format("NEQ %s %s", rd.string, rg.string);
            case GT_ZERO -> String.format("NEQ %s %s", rd.string, rg.string);
            case LT_ZERO -> String.format("NEQ %s %s", rd.string, rg.string);
            case GEQ_ZERO -> String.format("NEQ %s %s", rd.string, rg.string);
            case LEQ_ZERO -> String.format("NEQ %s %s", rd.string, rg.string);
            default -> String.format("UNKNOWN (0x%s)", toHex(getBytecode()));
        };
    }
}
