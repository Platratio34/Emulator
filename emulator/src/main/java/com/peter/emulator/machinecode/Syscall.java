package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Syscall extends Instruction {

    public final Operation operation;
    public InterruptOption interruptOption;

    protected Syscall(Operation operation, int data) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.data = data;
    }
    protected Syscall(Operation operation, InterruptOption interruptOption, int data) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.interruptOption = interruptOption;
        this.data = data;
    }

    public static Syscall Function(int id) {
        return new Syscall(Operation.FUNCTION, id & 0xffff);
    }
    public static Syscall Return() {
        return new Syscall(Operation.RETURN, 0);
    }
    public static Syscall Goto(int reg) {
        return new Syscall(Operation.GOTO, reg);
    }
    public static Syscall Interrupt(InterruptOption option, int val) {
        return new Syscall(Operation.GOTO, option, val & 0xff);
    }

    public static Syscall fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.SYSCALL.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        Syscall syscall = new Syscall(operation, bytecode & 0xffff);
        switch (operation) {
            case FUNCTION -> { }
            case RETURN -> {
                syscall.data = 0;
            }
            case GOTO -> { }
            case INTERRUPT -> {
                syscall.interruptOption = InterruptOption.fromBytecode(bytecode);
                syscall.data = bytecode & 0xff;
            }
        }
        return syscall;
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | (operation == Operation.INTERRUPT ? interruptOption.id : 0) | (data & 0xffff);
    }

    @Override
    public String toString() {
        switch(operation) {
            case FUNCTION -> { return String.format("SYSCALL 0x%04x", data); }
            case RETURN -> { return "SYSCALL RET"; }
            case GOTO -> { return String.format("SYSGOTO %s", MachineCode.translateReg(data)); }
            case INTERRUPT -> {
                return switch(interruptOption) {
                    case REGISTER -> String.format("INTERRUPT %s", MachineCode.translateReg(data));
                    case VALUE -> String.format("INTERRUPT %d", data);
                    case RETURN -> "INTERRUPT RET";
                };
            }
        }
        return String.format("SYSCALL UNKNOWN (0x%s)", toHex(getBytecode()));
    }

    public enum Operation {
        FUNCTION(0x00<<16),
        RETURN(0x01<<16),
        GOTO(0x02<<16),
        INTERRUPT(0x03<<16)
        ;

        public final int id;

        private Operation(int id) {
            this.id = id;
            setup();
        }

        protected static HashMap<Integer, Operation> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static Operation fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x00ff_0000, FUNCTION);
        }
    }

    public enum InterruptOption {
        REGISTER(0x00<<8),
        VALUE(0x01<<8),
        RETURN(0xff<<8)
        ;

        public final int id;

        private InterruptOption(int id) {
            this.id = id;
            setup();
        }

        protected static HashMap<Integer, InterruptOption> byId;

        private void setup() {
            if(byId == null)
                byId = new HashMap<>();
            byId.put(id, this);
        }

        public static InterruptOption fromBytecode(int bytecode) {
            return byId.getOrDefault(bytecode & 0x0000_ff00, REGISTER);
        }
    }
}
