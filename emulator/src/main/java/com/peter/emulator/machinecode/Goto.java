package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Goto extends Instruction {

    public final ConditionalOperator condition;
    public final Mode mode;
    public final int ra;
    public final int rg;
    public final boolean rel;

    public static int REL_MASK = 0b0001_0000 << 16;

    protected Goto(ConditionalOperator condition, Mode mode, int ra, int rg, boolean rel) {
        super(Operator.GOTO);
        this.condition = condition;
        this.mode = mode;
        if (rel) {
            this.ra = 0;
            data = ra;
        } else {
            this.ra = ra & 0xff;
        }
        this.rg = rg & 0xff;
        this.rel = rel;
    }

    public static Goto Unconditional(Mode mode, int ra) {
        return new Goto(ConditionalOperator.UNCONDITIONAL, mode, ra, 0, false);
    }

    public static Goto UnconditionalRelative(Mode mode, int offset) {
        return new Goto(ConditionalOperator.UNCONDITIONAL, mode, offset, 0, true);
    }

    public static Goto Conditional(ConditionalOperator condition, Mode mode, int ra, int rg) {
        return new Goto(condition, mode, ra, rg, false);
    }
    public static Goto ConditionalRelative(ConditionalOperator condition, Mode mode, int offset, int rg) {
        return new Goto(condition, mode, offset, rg, true);
    }

    public static Goto fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.GOTO.id) {
            return null;
        }
        ConditionalOperator condition = ConditionalOperator.fromBytecode(bytecode);
        Mode mode = Mode.fromBytecode(bytecode);
        if ((bytecode & REL_MASK) != 0) { // relative
            return new Goto(condition, mode, next, bytecode, true);
        }
        return new Goto(condition, mode, (bytecode >> 8), bytecode, false);
    }

    @Override
    public int getBytecode() {
        return op.id | mode.id | (rel ? REL_MASK : 0) | condition.id | (ra << 8) | rg;
    }

    @Override
    public boolean hasSecond() {
        return rel;
    }

    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public String toString() {
        String out = String.format("GOTO%s ", mode != Mode.NONE ? (" "+mode) : "");
        String raStr = rel ? (((data >= 0) ? "+" : "") + data) : MachineCode.translateReg(ra);
        return out + switch (condition) {
            case UNCONDITIONAL -> raStr;
            case EQ_ZERO -> String.format("EQ %s %s", MachineCode.translateReg(rg), raStr);
            case NEQ_ZERO -> String.format("NEQ %s %s", MachineCode.translateReg(rg), raStr);
            case GT_ZERO -> String.format("GT %s %s", MachineCode.translateReg(rg), raStr);
            case LT_ZERO -> String.format("LT %s %s", MachineCode.translateReg(rg), raStr);
            case GEQ_ZERO -> String.format("GEQ %s %s", MachineCode.translateReg(rg), raStr);
            case LEQ_ZERO -> String.format("LEQ %s %s", MachineCode.translateReg(rg), raStr);
            default -> String.format("UNKNOWN (0x%s)", toHex(getBytecode()));
        };
    }

    public enum Mode {
        NONE(0b000),
        PUSH(0b010),
        POP(0b100),
        ;

        public final int id;

        private Mode(int id) {
            this.id = id << 20;
            setup();
        }

        protected static HashMap<Integer, Mode> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Mode fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & (0b00110<<20), NONE);
        }
    }
}
