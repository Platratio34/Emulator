package com.peter.emulator.machinecode;

import java.util.HashMap;

public class StackInstruction extends Instruction {

    public final Operation operation;
    public final Reg rg;

    protected StackInstruction(Operation operation, Reg rg) {
        super(Operator.STACK);
        this.operation = operation;
        this.rg = rg;
    }
    protected StackInstruction(Operation operation, int data) {
        super(Operator.STACK);
        this.operation = operation;
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
        return new StackInstruction(Operation.PUSH, rg);
    }
    public static StackInstruction Pop(Reg rg) {
        return new StackInstruction(Operation.POP, rg);
    }

    public static StackInstruction fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STACK.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        switch (operation) {
            case PUSH, POP -> {
                return new StackInstruction(operation, Reg.from(bytecode));
            }
            case DEC, INC -> {
                return new StackInstruction(operation, bytecode & 0xffff);
            }
        }
        return null;
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | rg.code | this.data;
    }

    @Override
    public String toString() {
        return switch (operation) {
            case PUSH -> String.format("STACK PUSH %s", rg.string);
            case POP -> String.format("STACK POP %s", rg.string);
            
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
