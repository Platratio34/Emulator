package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Math extends Instruction {

    public final Operation operation;
    public final int rd;
    public final int ra;
    public final int rb;

    protected Math(Operation operation, int rd, int ra, int rb) {
        super(Operator.MATH);
        this.operation = operation;
        this.rd = rd & 0x0f;
        this.ra = ra & 0xff;
        this.rb = rb & 0xff;
    }
    protected Math(Operation operation, int rd, int val) {
        super(Operator.MATH);
        this.operation = operation;
        this.rd = rd & 0x0f;
        this.ra = 0;
        rb = val & 0xffff;
    }

    // public static Math Literal(int rg, int value) {
    //     return new Math(Operation.LITERAL, rg, value);
    // }
    // public static Math MemWord(int rg, int ra) {
    //     return new Math(Operation.MEM_WORD, rg, ra);
    // }
    // public static Math MemShort(int rg, int ra) {
    //     return new Math(Operation.MEM_SHORT, rg, ra);
    // }
    // public static Math MemByte(int rg, int ra) {
    //     return new Math(Operation.MEM_BYTE, rg, ra);
    // }
    public static Math Add(int rd, int ra, int rb) {
        return new Math(Operation.ADD, rd, ra, rb);
    }
    public static Math Sub(int rd, int ra, int rb) {
        return new Math(Operation.SUB, rd, ra, rb);
    }

    public static Math Inc(int rd, int amt) {
        if (amt < 0) {
            amt = (-amt & 0x7fff) | 0x8000;
        } else {
            amt = (amt - 1) & 0x7fff;
        }
        return new Math(Operation.INC, rd, amt);
    }

    public static Math And(int rd, int ra, int rb) {
        return new Math(Operation.AND, rd, ra, rb);
    }
    public static Math Or(int rd, int ra, int rb) {
        return new Math(Operation.OR, rd, ra, rb);
    }
    public static Math Nand(int rd, int ra, int rb) {
        return new Math(Operation.NAND, rd, ra, rb);
    }
    public static Math Nor(int rd, int ra, int rb) {
        return new Math(Operation.NOR, rd, ra, rb);
    }
    public static Math Not(int rd, int ra) {
        return new Math(Operation.NOT, rd, ra, 0);
    }
    public static Math Xor(int rd, int ra, int rb) {
        return new Math(Operation.XOR, rd, ra, rb);
    }

    public static Math LShift(int rd, int rg, int amt) {
        return new Math(Operation.LSHIFT, rd, rg, amt);
    }
    public static Math RShift(int rd, int rg, int amt) {
        return new Math(Operation.RSHIFT, rd, rg, amt);
    }

    public static Math Mul(int rd, int ra, int rb) {
        return new Math(Operation.MUL, rd, ra, rb);
    }
    public static Math Div(int rd, int ra, int rb) {
        return new Math(Operation.DIV, rd, ra, rb);
    }

    public static boolean inIncRange(int value) {
        return 0x8000 > value && value > -0x7ffff && value != 0;
    }

    public static Math fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.MATH.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        return switch(operation) {
            case INC -> new Math(operation, (bytecode >> 24) & 0x0f, bytecode & 0xffff);
            default -> new Math(operation, (bytecode >> 24) & 0x0f, (bytecode >> 16) & 0xff, bytecode & 0xff);
        };
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | (rd << 24) | (ra << 8) | rb;
    }

    public int getInc() {
        if ((rb & 0x8000) != 0) {
            return -(rb & 0x7fff);
        } else {
            return rb + 1;
        }
    }

    @Override
    public String toString() {
        return switch(operation) {
            case ADD, SUB, AND, OR, NAND, NOR, XOR, MUL, DIV -> String.format("%s %s %s %s", operation, MachineCode.translateReg(rd), MachineCode.translateReg(ra), MachineCode.translateReg(rb));

            case INC -> String.format("INC %s %d", MachineCode.translateReg(rd), getInc());

            case NOT -> String.format("NOT %s %s", MachineCode.translateReg(rd), MachineCode.translateReg(ra));

            case LSHIFT, RSHIFT -> String.format("%s %s %s %d", operation, MachineCode.translateReg(rd), MachineCode.translateReg(ra), rb);
            default -> String.format("MATH UNKNOWN (0x%08x)", getBytecode());
        };
    }

    public enum Operation {
        ADD(0x10 << 16),
        SUB(0x20 << 16),
        INC(0x30 << 16),
        AND(0x40 << 16),
        OR(0x50 << 16),
        NAND(0x60 << 16),
        NOR(0x70 << 16),
        NOT(0x80 << 16),
        XOR(0x90 << 16),
        LSHIFT(0xa0 << 16),
        RSHIFT(0xb0 << 16),
        MUL(0xc0 << 16),
        DIV(0xd0 << 16),
        UNKNOWN(0xf0 << 16);
        ;

        public final int id;

        private Operation(int id) {
            this.id = id;
            setup();
        }

        protected static HashMap<Integer, Operation> byId = null;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Operation fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x00ff_0000, UNKNOWN);
        }
    }
}
