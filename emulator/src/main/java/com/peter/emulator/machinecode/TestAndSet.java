package com.peter.emulator.machinecode;

public class TestAndSet extends Instruction {

    public final Reg rg;
    public final Reg ra;
    
    public final Mode mode;

    public TestAndSet(Reg rg, Reg ra) {
        super(Operator.TEST_AND_SET);
        this.rg = rg;
        this.ra = ra;
        this.mode = Mode.REG_ADDRESS;
    }
    public TestAndSet(Reg rg, int address) {
        super(Operator.TEST_AND_SET);
        this.rg = rg;
        this.ra = Reg.R0;
        data = address;
        this.mode = Mode.LIT_ADDRESS;
    }

    public static Instruction fromBytecode(int bytecode, int next) {
        Mode mode = Mode.fromBytecode(bytecode);
        Reg rg = Reg.from(bytecode >> 8);
        return switch (mode) {
            case REG_ADDRESS -> new TestAndSet(rg, Reg.from(bytecode));
            case LIT_ADDRESS -> new TestAndSet(rg, next);
        };
    }

    @Override
    public String toString() {
        return switch(mode) {
            case REG_ADDRESS -> String.format("TEST AND SET %s ? mem[%s]", rg, ra);
            case LIT_ADDRESS -> String.format("TEST AND SET %s ? mem[0x%s]", rg, toHex(data));
        };
    }

    @Override
    public int getBytecode() {
        return op.id | mode.id | (rg.code << 8) | ra.code;
    }

    @Override
    public boolean hasSecond() {
        return mode == Mode.LIT_ADDRESS;
    }

    @Override
    public int getSecondBytecode() {
        return data;
    }

    public enum Mode {
        REG_ADDRESS(0b0),
        LIT_ADDRESS(0b1)
        ;

        public final int id;

        private Mode(int id) {
            this.id = id << 23;
        }

        public static Mode fromBytecode(int bytecode) {
            return switch ((bytecode >> 23) & 0b1) {
                case 0 -> REG_ADDRESS;
                case 1 -> LIT_ADDRESS;
                default -> REG_ADDRESS;
            };
        }
    }

}
