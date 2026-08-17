package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Stack extends Instruction {

    public final Operation operation;
    public final int rg;

    protected Stack(Operation operation, int rg) {
        super(Operator.STACK);
        this.operation = operation;
        switch (operation) {
            case PUSH, POP -> {
                this.rg = rg & 0xff;
            }
            case INC, DEC, UNKNOWN -> {
                this.rg = rg & 0xffff;
            }
            default -> {
                this.rg = rg;
            }
        }
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
        if (amt > 0) {
            return Dec(amt);
        }
        return new Stack(Operation.DEC, amt - 1);
    }
    
    public static Stack Push(int rg) {
        return new Stack(Operation.PUSH, rg);
    }
    public static Stack Pop(int rg) {
        return new Stack(Operation.POP, rg);
    }

    public static Stack fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.STACK.id) {
            return null;
        }
        return new Stack(Operation.fromBytecode(bytecode), bytecode);
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | rg;
    }

    @Override
    public String toString() {
        return switch (operation) {
            case PUSH -> String.format("STACK PUSH %s", MachineCode.translateReg(rg));
            case POP -> String.format("STACK POP %s", MachineCode.translateReg(rg));
            
            case INC -> String.format("STACK INC %d", getInc());
            case DEC -> String.format("STACK DEC %d", getInc());
            
            default -> String.format("STACK UNKNOWN (0x%s)", toHex(getBytecode(), 6));
        };
    }
    
    public int getInc() {
        return rg + 1;
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
