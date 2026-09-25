package com.peter.emulator.machinecode;

import java.util.HashMap;

public abstract class Instruction {

    public final Operator op;
    public int data = 0;

    public Instruction(Operator op) {
        this.op = op;
    }
    public Instruction(Operator op, int data) {
        this.op = op;
        this.data = data;
    }

    public int getBytecode() {
        return op.id | (data & 0xff_ffff);
    }

    public static Instruction fromBytecode(int bytecode, int next) {
        Operator operator = Operator.fromBytecode(bytecode);
        if (operator == null) {
            return new Unknown(bytecode);
        }
        if (operator.supplier != null) {
            Instruction instr = operator.supplier.apply(bytecode, next);
            if (instr == null) {
                System.out.println(operator);
            }
            return instr;
        }
        return new Generic(bytecode);
    }
    public boolean hasSecond() {
        return false;
    }

    public int getSecondBytecode() {
        return 0;
    }
    
    public abstract String getASM();

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
            return String.format("0x%02x %02x %02x %02x", data >> 24, (data >> 16) & 0xff, (data >> 8) & 0xff,
                    data & 0xff);
        }
        
        @Override
        public String getASM() {
            return toString();
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

        @Override
        public String getASM() {
            return toString();
        }
    }

    public static Instruction Halt() {
        return new Instruction(Operator.HALT, 0xff_ffff) {
            @Override
            public String getASM() {
                return "HALT";
            }
        };
    }
    public static Instruction NoOp() {
        return new Instruction(Operator.NO_OP, 0x0) {
            @Override
            public String getASM() {
                return "NO_OP";
            }
        };
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
