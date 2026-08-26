package com.peter.emulator.machinecode;

public class Load extends Instruction {

    public static int FLAG_INC_RA = 0b1000_0000 << 8;

    public final Mode mode;
    public final Reg rg;
    public final Reg ra;
    public final boolean incRA;

    protected Load(Mode mode, Reg rg, Reg ra, boolean incRA) {
        super(Operator.LOAD);
        this.mode = mode;
        this.rg = rg;
        this.ra = ra;
        this.incRA = incRA;
    }

    protected Load(Reg rg, int value) {
        super(Operator.LOAD);
        this.mode = Mode.LITERAL;
        this.rg = rg;
        data = value;
        this.ra = Reg.R0;
        this.incRA = false;
    }

    public static Load Literal(Reg rg, int value) {
        return new Load(rg, value);
    }
    public static Load MemWord(Reg rg, Reg ra, boolean incRA) {
        return new Load(Mode.MEM_WORD, rg, ra, incRA);
    }
    public static Load MemShort(Reg rg, Reg ra, boolean incRA) {
        return new Load(Mode.MEM_SHORT, rg, ra, incRA);
    }

    public static Load MemByte(Reg rg, Reg ra, boolean incRA) {
        return new Load(Mode.MEM_BYTE, rg, ra, incRA);
    }

    public static Load fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.LOAD.id) {
            return null;
        }
        Reg rg = Reg.from(bytecode >> 16);
        Reg ra = Reg.from(bytecode);
        boolean incRA = (bytecode & FLAG_INC_RA) != 0;
        return switch(Mode.fromBytecode(bytecode)) {
            case LITERAL -> Literal(rg, next);
            case MEM_WORD -> MemWord(rg, ra, incRA);
            case MEM_SHORT -> MemShort(rg, ra, incRA);
            case MEM_BYTE -> MemByte(rg, ra, incRA);
        };
    }

    @Override
    public boolean hasSecond() {
        return mode == Mode.LITERAL;
    }

    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public int getBytecode() {
        return op.id | (rg.code << 16) | (incRA ? FLAG_INC_RA : 0) | mode.id | ra.code;
    }

    @Override
    public String toString() {
        return switch(mode) {
            case LITERAL -> String.format("LOAD %s <- 0x%s", rg.string, toHex(data));
            case MEM_WORD -> String.format("LOAD MEM WORD %s <- mem[%s]", rg.string, ra.string);
            case MEM_SHORT -> String.format("LOAD MEM SHORT %s <- mem[%s]", rg.string, ra.string);
            case MEM_BYTE -> String.format("LOAD MEM BYTE %s <- mem[%s]", rg.string, ra.string);
            default -> String.format("LOAD UNKNOWN (0x%s)", toHex(getBytecode()));
        } + (incRA ? " INC_RA" : "");
    }

    public enum Mode {
        LITERAL(0b00),
        MEM_WORD(0b01),
        MEM_SHORT(0b10),
        MEM_BYTE(0b11)
        ;

        public final int id;

        private Mode(int id) {
            this.id = id << 8;
            setup();
        }

        protected static Mode[] byId;

        private void setup() {
            if (byId == null) {
                byId = new Mode[4];
            }
            byId[id >> 8] = this;
        }

        public static Mode fromBytecode(int bytecode) {
            return byId[(bytecode >> 8) & 0b11];
        }
    }
}
