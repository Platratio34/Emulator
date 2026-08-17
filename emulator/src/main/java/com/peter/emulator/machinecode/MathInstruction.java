package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class MathInstruction extends Instruction {

    public final Operation operation;
    public final int rd;
    public final int ra;
    public final int rb;
    public final boolean rotate;

    public static final int ROTATE_FLAG = 0x80;

    protected MathInstruction(Operation operation, int rd, int ra, int rb) {
        super(Operator.MATH);
        this.operation = operation;
        this.rd = rd & 0x0f;
        this.ra = ra & 0xff;
        this.rb = rb & 0xff;
        rotate = false;
    }
    protected MathInstruction(Operation operation, int rd, int ra, int rb, boolean rotate) {
        super(Operator.MATH);
        this.operation = operation;
        this.rd = rd & 0x0f;
        this.ra = ra & 0xff;
        this.rb = rb & 0xff;
        this.rotate = false;
    }
    protected MathInstruction(Operation operation, int rd, int val) {
        super(Operator.MATH);
        this.operation = operation;
        this.rd = rd & 0x0f;
        this.ra = 0;
        rb = val & 0xffff;
        rotate = false;
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
    public static MathInstruction Add(int rd, int ra, int rb) {
        return new MathInstruction(Operation.ADD, rd, ra, rb);
    }
    public static MathInstruction Sub(int rd, int ra, int rb) {
        return new MathInstruction(Operation.SUB, rd, ra, rb);
    }

    public static MathInstruction Inc(int rd, int amt) {
        if (amt < 0) {
            amt = ((-amt) & 0x7fff) | 0x8000;
        } else {
            amt = (amt - 1) & 0x7fff;
        }
        return new MathInstruction(Operation.INC, rd, amt);
    }

    public static MathInstruction And(int rd, int ra, int rb) {
        return new MathInstruction(Operation.AND, rd, ra, rb);
    }
    public static MathInstruction Or(int rd, int ra, int rb) {
        return new MathInstruction(Operation.OR, rd, ra, rb);
    }
    public static MathInstruction Nand(int rd, int ra, int rb) {
        return new MathInstruction(Operation.NAND, rd, ra, rb);
    }
    public static MathInstruction Nor(int rd, int ra, int rb) {
        return new MathInstruction(Operation.NOR, rd, ra, rb);
    }
    public static MathInstruction Not(int rd, int ra) {
        return new MathInstruction(Operation.NOT, rd, ra, 0);
    }
    public static MathInstruction Xor(int rd, int ra, int rb) {
        return new MathInstruction(Operation.XOR, rd, ra, rb);
    }

    public static MathInstruction LShift(int rd, int rg, int amt) {
        return new MathInstruction(Operation.LSHIFT, rd, rg, amt & 0x7f);
    }

    public static MathInstruction RShift(int rd, int rg, int amt) {
        return new MathInstruction(Operation.RSHIFT, rd, rg, amt & 0x7f);
    }

    public static MathInstruction LRotate(int rd, int rg, int amt) {
        return new MathInstruction(Operation.LSHIFT, rd, rg, amt, true);
    }

    public static MathInstruction RRotate(int rd, int rg, int amt) {
        return new MathInstruction(Operation.RSHIFT, rd, rg, amt, true);
    }

    public static MathInstruction Mul(int rd, int ra, int rb) {
        return new MathInstruction(Operation.MUL, rd, ra, rb);
    }
    public static MathInstruction Div(int rd, int ra, int rb) {
        return new MathInstruction(Operation.DIV, rd, ra, rb);
    }

    public static boolean inIncRange(int value) {
        return 0x8000 > value && value > -0x7ffff && value != 0;
    }

    public static MathInstruction fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.MATH.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        return switch(operation) {
            case INC -> new MathInstruction(operation, (bytecode >> 16), bytecode);
            case LSHIFT, RSHIFT -> new MathInstruction(operation, (bytecode >> 16), (bytecode >> 8), bytecode & 0x7f, (bytecode & ROTATE_FLAG) != 0);
            default -> new MathInstruction(operation, (bytecode >> 16), (bytecode >> 8), bytecode);
        };
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | (rd << 16) | (ra << 8) | (rotate ? ROTATE_FLAG : 0) | rb;
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
        ADD(0x1),
        SUB(0x2),
        INC(0x3),
        AND(0x4),
        OR(0x5),
        NAND(0x6),
        NOR(0x7),
        NOT(0x8),
        XOR(0x9),
        LSHIFT(0xa),
        RSHIFT(0xb),
        MUL(0xc),
        DIV(0xd),
        UNKNOWN(0xf);
        ;

        public final int id;

        private Operation(int id) {
            this.id = id << 20;
            setup();
        }

        protected static HashMap<Integer, Operation> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Operation fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x00f0_0000, UNKNOWN);
        }
    }
}
