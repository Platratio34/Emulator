package com.peter.emulator.machinecode;

import java.util.HashMap;

public class Stack extends Instruction {

    public final Operation operation;
    public final Reg rg;

    protected Stack(Operation operation, Reg rg) {
        super(Operator.STACK);
        this.operation = operation;
        this.rg = rg;
    }
    protected Stack(Operation operation, int data) {
        super(Operator.STACK);
        this.operation = operation;
        this.rg = Reg.R0;
        this.data = data;
    }

    public static Stack Inc(int amt) {
        if(amt == 0) {
            throw new IllegalArgumentException("Amount mut bet non-zero");
        }
        if (amt < 0) {
            return Dec(amt);
        }
        return new Stack(Operation.INC, amt - 1);
    }

    public static Stack Dec(int amt) {
        if (amt == 0) {
            throw new IllegalArgumentException("Amount mut bet non-zero");
        }
        if (amt < 0) {
            return Inc(amt);
        }
        return new Stack(Operation.DEC, amt - 1);
    }
    
    public static Stack Push(Reg rg) {
        return new Stack(Operation.PUSH, rg);
    }
    public static Stack Pop(Reg rg) {
        return new Stack(Operation.POP, rg);
    }

    public static Stack fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STACK.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        switch (operation) {
            case PUSH, POP -> {
                return new Stack(operation, Reg.from(bytecode));
            }
            case DEC, INC, UNKNOWN -> {
                return new Stack(operation, bytecode & 0xffff);
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
        UNKNOWN(0x0),
                
        PUSH(0x00),
        POP(0x01),
        INC(0x02),
        DEC(0x03)
        ;

        public final int id;

        private Operation(int id) {
            this.id = id << 16;
            setup();
        }

        protected static HashMap<Integer, Operation> byId;

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
