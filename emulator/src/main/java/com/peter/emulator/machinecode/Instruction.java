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
            return String.format("0x%02x %02x %02x %02x", data >> 24, (data >> 16) & 0xff, (data >> 8) & 0xff, data & 0xff);
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
            return String.format("%s 0x%s", op, toHex(data & 0xff_ffff, 6));
        }
    }

    public static enum Operator {
        NO_OP(0x00, Generic::fromBytecode),
        LOAD(0x01, Load::fromBytecode),
        STORE(0x02, Store::fromBytecode),

        MATH(0x04, Math::fromBytecode),
        GOTO(0x05, Goto::fromBytecode),
        SET(0x06, Set::fromBytecode),

        STACK(0x10, Stack::fromBytecode),
        SYSCALL(0x11, Syscall::fromBytecode),

        HALT(0xff, Generic::fromBytecode),

        UNKNOWN(-1, null);

        public final int id;
        public final BiFunction<Integer, Integer, Instruction> supplier;

        private Operator(int id, BiFunction<Integer, Integer, Instruction> supplier) {
            if (id == -1) {
                this.id = 0xffff_ffff;
                this.supplier = Unknown::fromBytecode;
                setup();
                return;
            }
            this.id = id << 24;
            this.supplier = supplier;

            setup();
        }

        protected static HashMap<Integer, Operator> byId;

        private void setup() {
            if (byId == null) {
                byId = new HashMap<>();
            }
            byId.put(id, this);
        }

        public static Operator fromBytecode(int bytecode) {
            // System.out.println(toHexLead(bytecode & 0xff00_0000));
            return byId.getOrDefault(bytecode & 0xff00_0000, UNKNOWN);
        }
    }
    
    public static String toHex(int num) {
        String str = String.format("%08x", num);
        return str.substring(0, 4) + "_" + str.substring(4);
    }

    public static String toHex(int num, int digits) {
        String str = String.format("%0" + digits + "x", num);
        if (str.length() <= 4)
            return str;
        int bI = str.length() - 4;
        return str.substring(0, bI) + "_" + str.substring(bI);
    }

    public static String toHexLead(int num) {
        String str = String.format("%08x", num);
        return "0x" + str.substring(0, 4) + "_" + str.substring(4);
    }
    public static String toHexLead(int num, int digits) {
        String str = String.format("%0" + digits + "x", num);
        if (str.length() <= 4)
            return "0x" + str;
        int bI = str.length() - 4;
        return "0x" + str.substring(0, bI) + "_" + str.substring(bI);
    }
}
