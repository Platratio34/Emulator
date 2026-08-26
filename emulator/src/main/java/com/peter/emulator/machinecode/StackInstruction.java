package com.peter.emulator.machinecode;

import java.util.HashMap;

public class StackInstruction extends Instruction {

    public final Operation operation;
    public final MemorySize size;
    public final Reg rg;

    protected StackInstruction(Operation operation, MemorySize size, Reg rg) {
        super(Operator.STACK);
        this.operation = operation;
        this.size = size;
        this.rg = rg;
    }
    protected StackInstruction(Operation operation, int data) {
        super(Operator.STACK);
        this.operation = operation;
        this.size = MemorySize.WORD;
        this.rg = Reg.R0;
        this.data = data;
    }

    public static StackInstruction Inc(int amt) {
        if(amt == 0) {
            throw new IllegalArgumentException("Amount mut bet non-zero");
        }
        if (amt < 0) {
            return Dec(amt);
        }
        return new StackInstruction(Operation.INC, amt - 1);
    }

    public static StackInstruction Dec(int amt) {
        if (amt == 0) {
            throw new IllegalArgumentException("Amount mut bet non-zero");
        }
        if (amt < 0) {
            return Inc(amt);
        }
        return new StackInstruction(Operation.DEC, amt - 1);
    }
    
    public static StackInstruction Push(Reg rg) {
        return new StackInstruction(Operation.PUSH, MemorySize.WORD, rg);
    }

    public static StackInstruction Pop(Reg rg) {
        return new StackInstruction(Operation.POP, MemorySize.WORD, rg);
    }
    
    public static StackInstruction Push(MemorySize size, Reg rg) {
        return new StackInstruction(Operation.PUSH, size, rg);
    }
    public static StackInstruction Pop(MemorySize size, Reg rg) {
        return new StackInstruction(Operation.POP, size, rg);
    }

    public static StackInstruction fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STACK.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        switch (operation) {
            case PUSH, POP -> {
                return new StackInstruction(operation, MemorySize.fromBytecode(bytecode >> 8), Reg.from(bytecode));
            }
            case DEC, INC -> {
                return new StackInstruction(operation, bytecode & 0xffff);
            }
        }
        return null;
    }

    @Override
    public int getBytecode() {
        // |   0-7 |     8-15 | 16-23 | 24-31 |
        // | STACK | PUSH/POP |  size |    rg |
        // |   0-7 |    8-15 |  16-31 |
        // | STACK | INC/DEC | amount |
        return op.id | operation.id | (size.id << 8) | rg.code | this.data;
    }

    @Override
    public String toString() {
        return switch (operation) {
            case PUSH -> String.format("STACK PUSH %s %s", size, rg.string);
            case POP -> String.format("STACK POP %s %s", size, rg.string);
            
            case INC -> String.format("STACK INC %d", getInc());
            case DEC -> String.format("STACK DEC %d", getInc());
            
            default -> String.format("STACK UNKNOWN (0x%s)", toHex(getBytecode(), 6));
        };
    }
    
    public int getInc() {
        return data + 1;
    }

    public enum Operation {
        PUSH(0b00),
        POP(0b01),
        INC(0b10),
        DEC(0b11)
        ;

        public final int id;

        private Operation(int id) {
            this.id = id << 16;
        }

        public static Operation fromBytecode(int bytecode) {
            return switch((bytecode >> 16) & 0b11) {
                case 0b00 -> PUSH;
                case 0b01 -> POP;
                case 0b10 -> INC;
                case 0b11 -> DEC;
                
                default -> PUSH;
            };
        }
    }
}
