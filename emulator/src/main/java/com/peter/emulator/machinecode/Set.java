package com.peter.emulator.machinecode;

import com.peter.emulator.MachineCode;

public class Set extends Instruction {

    public final ConditionalOperator condition;
    public final int ra;
    public final int rg;
    public final boolean forced;

    public static int FORCE_MASK = 0x0010_0000;

    protected Set(ConditionalOperator condition, int ra, int rg, boolean forced) {
        super(Operator.SET);
        this.condition = condition;
        this.ra = ra & 0xff;
        this.rg = rg & 0xff;
        this.forced = forced;
    }

    public static Set NonForced(ConditionalOperator condition, int ra, int rg) {
        return new Set(condition, ra, rg, false);
    }
    public static Set Forced(ConditionalOperator condition, int offset, int rg) {
        return new Set(condition, offset, rg, true);
    }

    public static Set fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.SET.id) {
            return null;
        }
        return new Set(ConditionalOperator.fromBytecode(bytecode), (bytecode >> 8), bytecode, (bytecode & FORCE_MASK) != 0);
    }

    @Override
    public int getBytecode() {
        return op.id | (forced ? FORCE_MASK : 0) | condition.id | (ra << 8) | rg;
    }

    @Override
    public boolean hasSecond() {
        return forced;
    }

    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public String toString() {
        String out = String.format("SET%s ", forced ? " FORCE" : "");
        return out + switch (condition) {
            case UNCONDITIONAL -> MachineCode.translateReg(ra);
            case EQ_ZERO -> String.format("EQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case NEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case GT_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case LT_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case GEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case LEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            default -> String.format("UNKNOWN (0x%s)", toHex(getBytecode()));
        };
    }
}
