package com.peter.emulator.machinecode;

import java.util.HashMap;
import java.util.function.BiFunction;

public class Instruction {

    public final Operator op;
    public int data = 0;

    public Instruction(Operator op) {
        this.op = op;
    }

    public int getBytecode() {
        return op.id | (data & 0xff_ffff);
    }

    public static Instruction fromBytecode(int bytecode, int next) {
        Operator operator = Operator.fromBytecode(bytecode);
        if(operator.supplier != null) {
            return operator.supplier.apply(bytecode, next);
        }
        return new Generic(bytecode);
    }
    public boolean hasSecond() {
        return false;
    }
    public int getSecondBytecode() {
        return 0;
    }

    public static class Unknown extends Instruction {

        public Unknown(int bytecode) {
            super(Operator.UNKNOWN);
            data = bytecode;
        }

        public static Unknown fromBytecode(int bytecode, int next) {
            return new Unknown(bytecode);
        }

        @Override
        public String toString() {
            return String.format("0x%02x 0x%06x", data >> 24, data & 0xff_ffff);
        }
    }

    public static class Generic extends Instruction {
        public Generic(int bytecode) {
            super(Operator.fromBytecode(bytecode));
            data = bytecode;
        }

        public static Generic fromBytecode(int bytecode, int next) {
            return new Generic(bytecode);
        }

        @Override
        public String toString() {
            return String.format("%s 0x%06x", op, data & 0xff_ffff);
        }
    }

    public enum Operator {
        NO_OP(0x00, Generic::fromBytecode),
        LOAD(0x01, Load::fromBytecode),
        STORE(0x02, Store::fromBytecode),
        
        MATH(0x04, Math::fromBytecode),
        GOTO(0x05, null),
        SET(0x06, null),
        
        STACK(0x10, null),
        SYSCALL(0x11, Syscall::fromBytecode),
        
        HALT(0xff, Generic::fromBytecode),

        UNKNOWN(-1, null)
        ;

        public final int id;
        public final BiFunction<Integer, Integer, Instruction> supplier;

        private Operator(int id, BiFunction<Integer, Integer, Instruction> supplier) {
            if(id == -1) {
                this.id = 0;
                this.supplier = Unknown::fromBytecode;
                return;
            }
            this.id = id << 24;
            this.supplier = supplier;

            setup();
        }

        protected static HashMap<Integer, Operator> byId = null;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Operator fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0xff00_0000, UNKNOWN);
        }
    }
}
