package com.peter.emulator.machinecode;

import java.util.HashMap;

public class Goto extends Instruction {

    public final ConditionalOperator condition;
    public final Mode mode;
    public final Reg ra;
    public final Reg rg;
    public final boolean rel;

    public static final int REL_MASK = 0b0001_0000 << 16;

    protected Goto(ConditionalOperator condition, Mode mode, Reg ra, Reg rg) {
        super(Operator.GOTO);
        this.condition = condition;
        this.mode = mode;
        this.ra = ra;
        this.rg = rg;
        this.rel = false;
    }
    protected Goto(ConditionalOperator condition, Mode mode, int offset, Reg rg) {
        super(Operator.GOTO);
        this.condition = condition;
        this.mode = mode;
        this.ra = Reg.R0;
        data = offset;
        this.rg = rg;
        this.rel = true;
    }

    public static Goto Unconditional(Mode mode, Reg ra) {
        return new Goto(ConditionalOperator.UNCONDITIONAL, mode, ra, Reg.R0);
    }

    public static Goto UnconditionalRelative(Mode mode, int offset) {
        return new Goto(ConditionalOperator.UNCONDITIONAL, mode, offset, Reg.R0);
    }

    public static Goto Conditional(ConditionalOperator condition, Mode mode, Reg ra, Reg rg) {
        return new Goto(condition, mode, ra, rg);
    }
    public static Goto ConditionalRelative(ConditionalOperator condition, Mode mode, int offset, Reg rg) {
        return new Goto(condition, mode, offset, rg);
    }

    public static Goto Pop(ConditionalOperator condition, Reg rg) {
        return new Goto(condition, Mode.POP, Reg.R0, rg);
    }

    public static Goto fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.GOTO.id) {
            return null;
        }
        ConditionalOperator condition = ConditionalOperator.fromBytecode(bytecode);
        Mode mode = Mode.fromBytecode(bytecode);
        if ((bytecode & REL_MASK) != 0) { // relative
            return new Goto(condition, mode, next, Reg.from(bytecode));
        }
        return new Goto(condition, mode, Reg.from(bytecode >> 8), Reg.from(bytecode));
    }

    @Override
    public int getBytecode() {
        return op.id | mode.id | (rel ? REL_MASK : 0) | condition.id | (ra.code << 8) | rg.code;
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
        String raStr = rel ? (((data >= 0) ? "+" : "") + data) : ra.string;
        if (mode == Mode.POP) {
            raStr = "";
        }
        return out + switch (condition) {
            case UNCONDITIONAL -> raStr;
            case EQ_ZERO -> String.format("EQ %s %s", rg.string, raStr);
            case NEQ_ZERO -> String.format("NEQ %s %s", rg.string, raStr);
            case GT_ZERO -> String.format("GT %s %s", rg.string, raStr);
            case LT_ZERO -> String.format("LT %s %s", rg.string, raStr);
            case GEQ_ZERO -> String.format("GEQ %s %s", rg.string, raStr);
            case LEQ_ZERO -> String.format("LEQ %s %s", rg.string, raStr);
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
