package com.peter.emulator.machinecode;

public class StoreInstruction extends Instruction {

    public final MemorySize size;
    public final Source source;
    
    public final Reg rg;
    public final Reg ra;

    public boolean incRG = false;
    public boolean incRA = false;

    public static final int INC_RG_FLAG = 0b1000_0000 << 8;
    public static final int INC_RA_FLAG = 0b0100_0000 << 8;

    protected StoreInstruction(MemorySize size, Source source, Reg rg, Reg ra) {
        super(Operator.STORE);
        this.size = size;
        this.source = source;
        this.rg = rg;
        this.ra = ra;
    }
    protected StoreInstruction(MemorySize size, Reg rg, int address) {
        super(Operator.STORE);
        this.size = size;
        this.source = Source.ADDR;
        this.rg = rg;
        this.ra = Reg.R0;
        this.data = address;
    }
    protected StoreInstruction(MemorySize size, int value, Reg ra) {
        super(Operator.STORE);
        this.size = size;
        this.source = Source.VAL;
        data = value;
        this.rg = Reg.R0;
        this.ra = ra;
    }
    protected StoreInstruction(MemorySize size, Source source, Reg rg, Reg ra, boolean incRG, boolean incRA) {
        super(Operator.STORE);
        this.size = size;
        this.source = source;
        this.rg = rg;
        this.ra = ra;
        this.incRG = incRG;
        this.incRA = incRA;
    }
    protected StoreInstruction(MemorySize size, int value, Reg ra, boolean incRG, boolean incRA) {
        super(Operator.STORE);
        this.size = size;
        this.source = Source.VAL;
        data = value;
        this.rg = Reg.R0;
        this.ra = ra;
        this.incRG = incRG;
        this.incRA = incRA;
    }

    public StoreInstruction withIncRG() {
        incRG = true;
        return this;
    }
    public StoreInstruction withIncRA() {
        incRA = true;
        return this;
    }

    public static StoreInstruction StoreReg(MemorySize size, Reg rg, Reg ra) {
        return new StoreInstruction(size, Source.REG, rg, ra);
    }
    public static StoreInstruction StoreReg(MemorySize size, Reg rg, Reg ra, boolean incRA) {
        StoreInstruction instr = new StoreInstruction(size, Source.REG, rg, ra);
        if (incRA)
            instr.withIncRA();
        return instr;
    }

    public static StoreInstruction StoreVal(MemorySize size, int val, Reg ra) {
        return new StoreInstruction(size, val, ra);
    }
    
    public static StoreInstruction StoreAddr(MemorySize size, Reg rg, int address) {
        return new StoreInstruction(size, rg, address);
    }

    // public static StoreInstruction CopyReg(Reg rs, Reg rd) {
    //     return new StoreInstruction(MemorySize.WORD, Source.REG_REG, rs, rd);
    // }
    public static StoreInstruction CopyMem(MemorySize size, Reg rs, Reg rd) {
        return new StoreInstruction(size, Source.MEM, rs, rd);
    }

    public static StoreInstruction fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STORE.id) {
            return null;
        }
        MemorySize size = MemorySize.fromBytecode(bytecode >> 8);
        Source source = Source.fromBytecode(bytecode);
        boolean incRG = (bytecode & INC_RG_FLAG) != 0;
        boolean incRA = (bytecode & INC_RA_FLAG) != 0;
        if (source == Source.VAL) {
            return new StoreInstruction(size, next, Reg.from(bytecode), incRG, incRA);
        }
        return new StoreInstruction(size, source, Reg.from(bytecode >> 16), Reg.from(bytecode), incRG, incRA);
    }

    @Override
    public int getBytecode() {
        return op.id | (rg.code << 16) | (incRG ? INC_RG_FLAG : 0) | (incRA ? INC_RA_FLAG : 0) | source.id | (size.id << 8) | ra.code;
    }

    @Override
    public boolean hasSecond() {
        return source == Source.VAL || source == Source.ADDR;
    }
    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public String toString() {
        String sizeStr = switch (size) {
            case WORD -> "";
            case SHORT -> " SHORT";
            case BYTE -> " BYTE";
        };
        String out = switch (source) {
            case REG -> String.format("STORE%s %s -> mem[%s]", sizeStr, rg.string, ra.string);
            case MEM -> String.format("COPY%s mem[%s] -> mem[%s]", sizeStr, rg.string, ra.string);
            case VAL -> String.format("STORE%s 0x%s -> mem[%s]", sizeStr, toHex(data), ra.string);
            case ADDR -> String.format("STORE%s %s -> mem[0x%s]", sizeStr, rg.string, toHex(data));

            default -> String.format("STORE UNKNOWN (0x%s)", toHex(getBytecode()));
        };
        if (incRG) {
            out += " INC_RG";
        }
        if (incRA) {
            out += " INC_RA";
        }
        return out;
    }
    
    @Override
    public String getASM() {
        String sizeStr = switch (size) {
            case WORD -> "";
            case SHORT -> " SHORT";
            case BYTE -> " BYTE";
        };
        String out = switch (source) {
            case REG -> String.format("STORE%s %s %s", sizeStr, rg, ra);
            case MEM -> String.format("COPY%s %s %s", sizeStr, rg, ra);
            case VAL -> String.format("STORE%s 0x%s %s", sizeStr, toHex(data), ra);
            case ADDR -> String.format("STORE%s %s 0x%s", sizeStr, rg, toHex(data));

            default -> String.format("STORE UNKNOWN (0x%s)", toHex(getBytecode()));
        };
        if (incRG) {
            out += " INC_RG";
        }
        if (incRA) {
            out += " INC_RA";
        }
        return out;
    }

    public enum Source {
        REG(0b00),
        VAL(0b01),
        MEM(0b10),
        ADDR(0b11)
        ;

        public final int id;

        private Source(int id) {
            this.id = id << 10;
        }

        public static Source fromBytecode(int bytecode) {
            return switch ((bytecode >> 10) & 0b11) {
                case 0b00 -> REG;
                case 0b01 -> VAL;
                case 0b10 -> MEM;
                case 0b11 -> ADDR;
                default -> REG;
            };
        }
    }
}
