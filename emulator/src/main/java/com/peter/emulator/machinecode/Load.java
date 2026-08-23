package com.peter.emulator.machinecode;

import java.util.HashMap;

public class Load extends Instruction {

    public final Mode mode;
    public final Reg rg;
    public final Reg ra;

    protected Load(Mode mode, Reg rg, Reg ra) {
        super(Operator.LOAD);
        this.mode = mode;
        this.rg = rg;
        this.ra = ra;
    }

    protected Load(Reg rg, int value) {
        super(Operator.LOAD);
        this.mode = Mode.LITERAL;
        this.rg = rg;
        data = value;
        this.ra = Reg.R0;
    }

    public static Load Literal(Reg rg, int value) {
        return new Load(rg, value);
    }
    public static Load MemWord(Reg rg, Reg ra) {
        return new Load(Mode.MEM_WORD, rg, ra);
    }
    public static Load MemShort(Reg rg, Reg ra) {
        return new Load(Mode.MEM_SHORT, rg, ra);
    }
    public static Load MemByte(Reg rg, Reg ra) {
        return new Load(Mode.MEM_BYTE, rg, ra);
    }

    public static Load fromBytecode(int bytecode, int next) {
        if ((bytecode & 0xff00_0000) != Operator.LOAD.id) {
            return null;
        }
        Reg rg = Reg.from(bytecode >> 16);
        Reg ra = Reg.from(bytecode);
        return switch(Mode.fromBytecode(bytecode)) {
            case LITERAL -> Literal(rg, next);
            case MEM_WORD, UNKNOWN -> MemWord(rg, ra);
            case MEM_SHORT -> MemShort(rg, ra);
            case MEM_BYTE -> MemByte(rg, ra);
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
        return op.id | (rg.code << 16) | mode.id | ra.code;
    }

    @Override
    public String toString() {
        return switch(mode) {
            case LITERAL -> String.format("LOAD %s <- 0x%s", rg.string, toHex(data));
            case MEM_WORD -> String.format("LOAD MEM WORD %s <- mem[%s]", rg.string, ra.string);
            case MEM_SHORT -> String.format("LOAD MEM SHORT %s <- mem[%s]", rg.string, ra.string);
            case MEM_BYTE -> String.format("LOAD MEM BYTE %s <- mem[%s]", rg.string, ra.string);
            default -> String.format("LOAD UNKNOWN (0x%s)", toHex(getBytecode()));
        };
    }

    public enum Mode {
        UNKNOWN(0x00),
                
        LITERAL(0x00),
        MEM_WORD(0x01),
        MEM_SHORT(0x02),
        MEM_BYTE(0x03)
        ;

        public final int id;

        private Mode(int id) {
            this.id = id << 8;
            setup();
        }

        protected static HashMap<Integer, Mode> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Mode fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x0000_0300, UNKNOWN);
        }
    }
}
