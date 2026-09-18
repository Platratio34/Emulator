package com.peter.emulator.machinecode;

public class Load extends Instruction {

    /*
    | 31-24 | 23-16 | 15-14 | 13-12 | 11     | 10-8 | 7-0 |
    | 0x01  | rg    | MODE  | SIZE  | INC_RA |      | ra  |
    
    | 31-24 | 23-16 | 15-14 | 13-12 | 11     | 10-8 | 7-0 |
    | 0x01  | rg    | MODE  | SIZE  | INC_RA |      | ra  |
    */

    public static int FLAG_INC_RA = 0b0000_1000 << 8;

    public final Mode mode;
    public final MemorySize size;
    public final Reg rg;
    public final Reg ra;
    public final boolean incRA;

    protected Load(MemorySize size, Reg rg, Reg ra, boolean incRA) {
        super(Operator.LOAD);
        this.mode = Mode.MEMORY;
        this.size = size;
        this.rg = rg;
        this.ra = ra;
        this.incRA = incRA;
    }

    protected Load(Reg rg, Reg ra) {
        super(Operator.LOAD);
        this.mode = Mode.COPY;
        this.size = MemorySize.WORD;
        this.rg = rg;
        this.ra = ra;
        this.incRA = false;
    }

    protected Load(Reg rg, int value) {
        super(Operator.LOAD);
        this.mode = Mode.LITERAL;
        this.size = MemorySize.WORD;
        this.rg = rg;
        data = value;
        this.ra = Reg.R0;
        this.incRA = false;
    }
    
    protected Load(MemorySize size, Reg rg, int value) {
        super(Operator.LOAD);
        this.mode = Mode.LITERAL_ADDRESS;
        this.size = size;
        this.rg = rg;
        data = value;
        this.ra = Reg.R0;
        this.incRA = false;
    }

    public static Load Literal(Reg rg, int value) {
        return new Load(rg, value);
    }

    public static Load Copy(Reg rs, Reg rd) {
        return new Load(rd, rs);
    }

    public static Load LiteralAddress(MemorySize size, Reg rg, int value) {
        return new Load(size, rg, value);
    }

    public static Load MemWord(Reg rg, Reg ra, boolean incRA) {
        return new Load(MemorySize.WORD, rg, ra, incRA);
    }
    public static Load MemShort(Reg rg, Reg ra, boolean incRA) {
        return new Load(MemorySize.SHORT, rg, ra, incRA);
    }

    public static Load MemByte(Reg rg, Reg ra, boolean incRA) {
        return new Load(MemorySize.BYTE, rg, ra, incRA);
    }

    public static Load fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.LOAD.id) {
            return null;
        }
        Reg rg = Reg.from(bytecode >> 16);
        Reg ra = Reg.from(bytecode);
        boolean incRA = (bytecode & FLAG_INC_RA) != 0;
        MemorySize size = MemorySize.fromBytecode(bytecode >> 12);
        return switch(Mode.fromBytecode(bytecode)) {
            case LITERAL -> new Load(rg, next);
            case COPY -> new Load(rg, ra);
            case MEMORY -> new Load(size, rg, ra, incRA);
            case LITERAL_ADDRESS -> new Load(size, rg, next);
        };
    }

    @Override
    public boolean hasSecond() {
        return mode == Mode.LITERAL || mode == Mode.LITERAL_ADDRESS;
    }

    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public int getBytecode() {
        return op.id | (rg.code << 16) | mode.id | (size.id << 12) | (incRA ? FLAG_INC_RA : 0) | ra.code;
    }

    @Override
    public String toString() {
        return switch(mode) {
            case LITERAL -> String.format("LOAD %s <- 0x%s", rg.string, toHex(data));
            case COPY -> String.format("COPY %s -> %s", ra, rg);

            case LITERAL_ADDRESS -> String.format("LOAD MEM %s %s <- mem[0x%s]", size, rg, toHex(data));
            case MEMORY -> String.format("LOAD MEM %s %s <- mem[%s]", size, rg, ra);

            default -> String.format("LOAD UNKNOWN (0x%s)", toHex(getBytecode()));
        } + (incRA ? " INC_RA" : "");
    }

    public enum Mode {
        LITERAL(0b00),
        COPY(0b01),
        LITERAL_ADDRESS(0b10),
        MEMORY(0b11)
        ;

        public final int id;

        private Mode(int id) {
            this.id = id << 14;
            setup();
        }

        protected static Mode[] byId;

        private void setup() {
            if (byId == null) {
                byId = new Mode[4];
            }
            byId[id >> 14] = this;
        }

        public static Mode fromBytecode(int bytecode) {
            return byId[(bytecode >> 14) & 0b11];
        }
    }
}
