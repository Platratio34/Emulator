package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Load extends Instruction {

    public final Mode mode;
    public final int rg;
    public final int ra;

    protected Load(Mode mode, int rg, int ra) {
        super(Operator.LOAD);
        this.mode = mode;
        this.rg = rg & 0xff;
        if(mode == Mode.LITERAL) {
            this.data = ra;
            this.ra = 0;
        } else {
            this.ra = ra & 0xff;
        }
    }

    public static Load Literal(int rg, int value) {
        return new Load(Mode.LITERAL, rg, value);
    }
    public static Load MemWord(int rg, int ra) {
        return new Load(Mode.MEM_WORD, rg, ra);
    }
    public static Load MemShort(int rg, int ra) {
        return new Load(Mode.MEM_SHORT, rg, ra);
    }
    public static Load MemByte(int rg, int ra) {
        return new Load(Mode.MEM_BYTE, rg, ra);
    }

    public static Load fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.LOAD.id) {
            return null;
        }
        return switch(Mode.fromBytecode(bytecode)) {
            case LITERAL -> Literal((bytecode >> 16) & 0xff, next);
            case MEM_WORD -> MemWord((bytecode >> 16) & 0xff, bytecode & 0xff);
            case MEM_SHORT -> MemShort((bytecode >> 16) & 0xff, bytecode & 0xff);
            case MEM_BYTE -> MemByte((bytecode >> 16) & 0xff, bytecode & 0xff);
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
        return op.id | (rg << 16) | mode.id | ra;
    }

    @Override
    public String toString() {
        return switch(mode) {
            case LITERAL -> String.format("LOAD %s 0x%08x", MachineCode.translateReg(rg), data);
            case MEM_WORD -> String.format("LOAD MEM WORD %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case MEM_SHORT -> String.format("LOAD MEM SHORT %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            case MEM_BYTE -> String.format("LOAD MEM BYTE %s %s", MachineCode.translateReg(rg), MachineCode.translateReg(ra));
            default -> String.format("LOAD UNKNOWN (0x%08x)", getBytecode());
        };
    }

    public enum Mode {
        LITERAL(0x00<<8),
        MEM_WORD(0x01<<8),
        MEM_SHORT(0x02<<8),
        MEM_BYTE(0x03<<8)
        ;

        public final int id;

        private Mode(int id) {
            this.id = id;
            setup();
        }

        protected static HashMap<Integer, Mode> byId = null;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Mode fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x00ff_0000, LITERAL);
        }
    }
}
