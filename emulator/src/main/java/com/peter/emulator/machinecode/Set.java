package com.peter.emulator.machinecode;

import com.peter.emulator.MachineCode;

public class Set extends Instruction {

    public final ConditionalOperator condition;
    public final int rg;
    public final int rd;
    public final boolean forced;

    public static int FORCE_MASK = 0x0010_0000;

    protected Set(ConditionalOperator condition, int rg, int rd, boolean forced) {
        super(Operator.SET);
        this.condition = condition;
        this.rg = rg & 0xff;
        this.rd = rd & 0xff;
        this.forced = forced;
    }

    public static Set NonForced(ConditionalOperator condition, int rg, int rd) {
        return new Set(condition, rg, rd, false);
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
        return op.id | (forced ? FORCE_MASK : 0) | condition.id | (rg << 8) | rd;
    }

    @Override
    public String toString() {
        String out = String.format("SET%s ", forced ? " FORCE" : "");
        return out + switch (condition) {
            case UNCONDITIONAL -> MachineCode.translateReg(rg);
            case EQ_ZERO -> String.format("EQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            case NEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            case GT_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            case LT_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            case GEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            case LEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(rg));
            default -> String.format("UNKNOWN (0x%s)", toHex(getBytecode()));
        };
    }
}
