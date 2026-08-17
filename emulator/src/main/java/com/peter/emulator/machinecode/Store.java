package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Store extends Instruction {

    public final Size size;
    public final Source source;
    
    public final int rg;
    public final int ra;

    public boolean incRG = false;
    public boolean incRA = false;

    public static final int INC_RG_FLAG = 0b1000_0000 << 8;
    public static final int INC_RA_FLAG = 0b0100_0000 << 8;

    protected Store(Size size, Source source, int rg, int ra) {
        super(Operator.STORE);
        this.size = size;
        this.source = source;
        if (source == Source.VAL) {
            data = rg;
            this.rg = 0;
        } else {
            this.rg = rg & 0xff;
        }
        this.ra = ra & 0xff;
    }
    protected Store(Size size, Source source, int rg, int ra, boolean incRG, boolean incRA) {
        super(Operator.STORE);
        this.size = size;
        this.source = source;
        if (source == Source.VAL) {
            data = rg;
            this.rg = 0;
        } else {
            this.rg = rg & 0xff;
        }
        this.ra = ra & 0xff;
        this.incRG = incRG;
        this.incRA = incRA;
    }

    public Store withIncRG() {
        incRG = true;
        return this;
    }
    public Store withIncRA() {
        incRA = true;
        return this;
    }

    public static Store StoreReg(Size size, int rg, int ra) {
        return new Store(size, Source.REG, rg, ra);
    }
    public static Store StoreVal(Size size, int val, int ra) {
        return new Store(size, Source.VAL, val, ra);
    }

    public static Store CopyReg(Size size, int rs, int rd) {
        return new Store(size, Source.REG_REG, rs, rd);
    }
    public static Store CopyMem(Size size, int rs, int rd) {
        return new Store(size, Source.MEM, rs, rd);
    }

    // public static Store Literal(int rg, int value) {
    //     return new Store(Mode.LITERAL, rg, value);
    // }
    // public static Store MemWord(int rg, int ra) {
    //     return new Store(Mode.MEM_WORD, rg, ra);
    // }
    // public static Store MemShort(int rg, int ra) {
    //     return new Store(Mode.MEM_SHORT, rg, ra);
    // }
    // public static Store MemByte(int rg, int ra) {
    //     return new Store(Mode.MEM_BYTE, rg, ra);
    // }

    public static Store fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STORE.id) {
            return null;
        }
        Size size = Size.fromBytecode(bytecode);
        Source source = Source.fromBytecode(bytecode);
        boolean incRG = (bytecode & INC_RG_FLAG) != 0;
        boolean incRA = (bytecode & INC_RA_FLAG) != 0;
        if (source == Source.VAL) {
            return new Store(size, source, next, bytecode, incRG, incRA);
        }
        return new Store(size, source, bytecode >> 16, bytecode, incRG, incRA);
    }

    @Override
    public int getBytecode() {
        return op.id | (rg << 16) | (incRG ? INC_RG_FLAG : 0) | (incRA ? INC_RA_FLAG : 0) | source.id | size.id | ra;
    }

    @Override
    public boolean hasSecond() {
        return source == Source.VAL;
    }
    @Override
    public int getSecondBytecode() {
        return data;
    }

    @Override
    public String toString() {
        String sizeStr = switch(size) {
            case WORD -> "";
            case SHORT -> " SHORT";
            case BYTE -> " BYTE";
        };
        String out =  switch(source) {
            case REG -> String.format("STORE%s %s -> mem[%s]", sizeStr, MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case MEM -> String.format("COPY%s mem[%s] -> mem[%s]", sizeStr, MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case VAL -> String.format("STORE%s 0x%s -> mem[%s]", sizeStr, toHex(data), MachineCode.translateReg(ra));
            case REG_REG -> String.format("COPY%s %s -> %s", sizeStr, MachineCode.translateReg(rg), MachineCode.translateReg(ra));

            default -> String.format("STORE UNKNOWN (0x%s)", toHex(getBytecode()));
        };
        if(incRG) {
            out += " INC_RG";
        }
        if(incRA) {
            out += " INC_RA";
        }
        return out;
    }

    public enum Size {
        WORD(0b00),
        SHORT(0b01),
        BYTE(0b10)
        ;

        public final int id;

        private Size(int id) {
            this.id = id << 8;
            setup();
        }

        protected static HashMap<Integer, Size> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Size fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0b11 << 8, WORD);
        }
    }

    public enum Source {
        REG(0b00),
        VAL(0b01),
        MEM(0b10),
        REG_REG(0b11)
        ;

        public final int id;

        private Source(int id) {
            this.id = id << 10;
            setup();
        }

        protected static HashMap<Integer, Source> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Source fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & (0b11 << 10), REG);
        }
    }
}
