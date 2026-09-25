package com.peter.emulator.machinecode;

public class MathInstruction extends Instruction {

    public final Operation operation;
    public final Reg rd;
    public final Reg ra;
    public final Reg rb;
    public final boolean rotate;

    public static final int ROTATE_FLAG = 0x80;

    public MathInstruction(Operation operation, Reg rd, Reg ra, Reg rb) {
        super(Operator.MATH);
        if(rd.code > 0xf) {
            throw new RuntimeException("Invalid destination register for math");
        }
        this.operation = operation;
        this.rd = rd;
        this.ra = ra;
        this.rb = rb;
        rotate = false;
    }
    protected MathInstruction(Operation operation, Reg rd, Reg ra, int amt, boolean rotate) {
        super(Operator.MATH);
        if(rd.code > 0xf) {
            throw new RuntimeException("Invalid destination register for math");
        }
        this.operation = operation;
        this.rd = rd;
        this.ra = ra;
        this.rb = Reg.R0;
        this.data = amt & 0x7f;
        this.rotate = rotate;
    }
    protected MathInstruction(Operation operation, Reg rd, Reg ra, int amt) {
        super(Operator.MATH);
        if(rd.code > 0xf) {
            throw new RuntimeException("Invalid destination register for math");
        }
        this.operation = operation;
        this.rd = rd;
        this.ra = ra;
        this.rb = Reg.R0;
        this.data = amt & 0xff;
        rotate = false;
    }
    protected MathInstruction(Operation operation, Reg rd, int val) {
        super(Operator.MATH);
        if(rd.code > 0xf) {
            throw new RuntimeException("Invalid destination register for math");
        }
        this.operation = operation;
        this.rd = rd;
        this.ra = Reg.R0;
        this.rb = Reg.R0;
        data = val & 0xffff;
        rotate = false;
    }

    public static MathInstruction Add(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.ADD, rd, ra, rb);
    }

    public static MathInstruction Sub(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.SUB, rd, ra, rb);
    }
    
    public static MathInstruction AddLit(Reg rd, Reg ra, int rb) {
        return new MathInstruction(Operation.ADD_LIT, rd, ra, rb);
    }
    public static MathInstruction SubLit(Reg rd, Reg ra, int rb) {
        return new MathInstruction(Operation.SUB_LIT, rd, ra, rb);
    }

    public static MathInstruction Inc(Reg rd, int amt) {
        if (amt < 0) {
            amt = ((-amt) & 0x7fff) | 0x8000;
        } else {
            amt = (amt - 1) & 0x7fff;
        }
        return new MathInstruction(Operation.INC, rd, amt);
    }

    public static MathInstruction And(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.AND, rd, ra, rb);
    }
    public static MathInstruction Or(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.OR, rd, ra, rb);
    }
    public static MathInstruction Nand(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.NAND, rd, ra, rb);
    }
    public static MathInstruction Nor(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.NOR, rd, ra, rb);
    }
    public static MathInstruction Not(Reg rd, Reg ra) {
        return new MathInstruction(Operation.NOT, rd, ra, Reg.R0);
    }
    public static MathInstruction Xor(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.XOR, rd, ra, rb);
    }

    public static MathInstruction LShift(Reg rd, Reg rg, int amt) {
        return new MathInstruction(Operation.LSHIFT, rd, rg, amt, false);
    }

    public static MathInstruction RShift(Reg rd, Reg rg, int amt) {
        return new MathInstruction(Operation.RSHIFT, rd, rg, amt, false);
    }

    public static MathInstruction LRotate(Reg rd, Reg rg, int amt) {
        return new MathInstruction(Operation.LSHIFT, rd, rg, amt, true);
    }

    public static MathInstruction RRotate(Reg rd, Reg rg, int amt) {
        return new MathInstruction(Operation.RSHIFT, rd, rg, amt, true);
    }

    public static MathInstruction Mul(Reg rd, Reg ra, Reg rb) {
        return new MathInstruction(Operation.MUL, rd, ra, rb);
    }
    public static MathInstruction Div(Reg rd, Reg ra, Reg rb) {
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
        Reg rd = Reg.from((bytecode >> 16) & 0xf);
        return switch(operation) {
            case INC -> new MathInstruction(operation, rd, bytecode);
            case LSHIFT, RSHIFT -> new MathInstruction(operation, rd, Reg.from(bytecode >> 8), bytecode, (bytecode & ROTATE_FLAG) != 0);
            case ADD_LIT, SUB_LIT -> new MathInstruction(operation, rd, Reg.from(bytecode >> 8), bytecode);
            default -> new MathInstruction(operation, rd, Reg.from(bytecode >> 8), Reg.from(bytecode));
        };
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | (rd.code << 16) | (ra.code << 8) | (rotate ? ROTATE_FLAG : 0) | rb.code | data;
    }

    public int getInc() {
        if ((data & 0x8000) != 0) {
            return -(data & 0x7fff);
        } else {
            return data + 1;
        }
    }

    @Override
    public String toString() {
        if (operation == Operation.LSHIFT && rotate) {
            return String.format("LRT %s %s %d", rd.string, ra.string, data);
        } else if (operation == Operation.RSHIFT && rotate) {
            return String.format("RRT %s %s %d", rd.string, ra.string, data);
        }
        return switch (operation) {
            case ADD, SUB, AND, OR, NAND, NOR, XOR, MUL, DIV ->
                String.format("%s %s %s %s", operation, rd.string, ra.string, rb.string);

            case INC -> String.format("INC %s %d", rd.string, getInc());

            case NOT -> String.format("NOT %s %s", rd.string, ra.string);

            case ADD_LIT -> String.format("ADD %s %s %d", rd.string, ra.string, data);
            case SUB_LIT -> String.format("SUB %s %s %d", rd.string, ra.string, data);

            case LSHIFT, RSHIFT -> String.format("%s %s %s %d", operation, rd.string, ra.string, data);
            default -> String.format("MATH UNKNOWN (0x%08x)", getBytecode());
        };
    }
    
    @Override
    public String getASM() {
        if (operation == Operation.LSHIFT) {
            if (rotate) {
                return String.format("LRT %s %s %d", rd, ra, data);
            }
            return String.format("LSH %s %s %d", rd, ra, data);
        } else if (operation == Operation.RSHIFT) {
            if (rotate) {
                return String.format("RRT %s %s %d", rd, ra, data);
            }
            return String.format("RSH %s %s %d", rd, ra, data);
        }
        return toString();
    }

    public enum Operation {
        NONE(0x0),
        ADD(0x1), // ADD rd ra rb
        SUB(0x2), // SUB rd ra rb
        INC(0x3), // INC rd amt
        AND(0x4), // AND rd ra rb
        OR(0x5), // OR rd ra rb
        NAND(0x6), // NAND rd ra rb
        NOR(0x7), // NOR rd ra rb
        NOT(0x8), // NOT rd ra
        XOR(0x9), // XOR rd ra rb
        LSHIFT(0xa), // LSHIFT rd ra amt
        RSHIFT(0xb), // RSHIFT rd ra amt
        MUL(0xc), // MUL rd ra rb
        DIV(0xd), // DIV rd ra rb
        ADD_LIT(0xe), // ADD_LIT rd ra amt
        SUB_LIT(0xf); // SUB_LIT rd ra amt
        ;

        public final int id;

        private Operation(int id) {
            this.id = id << 20;
            setup();
        }

        protected static Operation[] byId;

        private void setup() {
            if (byId == null) {
                byId = new Operation[16];
            }
            byId[id >> 20] = this;
        }

        public static Operation fromBytecode(int bytecode) {
            return byId[(bytecode >> 20) & 0xf];
        }
    }
}
