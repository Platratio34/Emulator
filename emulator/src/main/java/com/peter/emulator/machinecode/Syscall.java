package com.peter.emulator.machinecode;

import java.util.HashMap;

import com.peter.emulator.MachineCode;

public class Syscall extends Instruction {

    public final Operation operation;
    public final Reg rg;
    public InterruptOption interruptOption;

    protected Syscall(Operation operation, int data) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.data = data;
        this.rg = Reg.R0;
    }
    protected Syscall(Operation operation, Reg rg) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.rg = rg;
    }
    protected Syscall(Operation operation, InterruptOption interruptOption, int data) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.interruptOption = interruptOption;
        this.data = data;
        this.rg = Reg.R0;
    }
    protected Syscall(Operation operation, InterruptOption interruptOption, Reg rg) {
        super(Operator.SYSCALL);
        this.operation = operation;
        this.interruptOption = interruptOption;
        this.rg = rg;

    }

    public static Syscall Function(int id) {
        return new Syscall(Operation.FUNCTION, id & 0xffff);
    }
    public static Syscall Return() {
        return new Syscall(Operation.RETURN, 0);
    }
    public static Syscall Goto(Reg reg) {
        return new Syscall(Operation.GOTO, reg);
    }

    public static Syscall InterruptReturn() {
        return new Syscall(Operation.INTERRUPT, InterruptOption.RETURN, Reg.R0);
    }
    public static Syscall Interrupt(int val) {
        return new Syscall(Operation.INTERRUPT, InterruptOption.VALUE, val & 0xff);
    }
    public static Syscall Interrupt(Reg rg) {
        return new Syscall(Operation.INTERRUPT, InterruptOption.REGISTER, rg);
    }
    
    public static Syscall Translate(Reg reg) {
        return new Syscall(Operation.TRANSLATE, reg);
    }

    public static Syscall fromBytecode(int bytecode, int next) {
        if((bytecode & 0xff00_0000) != Operator.SYSCALL.id) {
            return null;
        }
        Operation operation = Operation.fromBytecode(bytecode);
        switch (operation) {
            case FUNCTION -> {
                return new Syscall(operation, bytecode & 0xffff);
            }
            case RETURN -> {
                new Syscall(operation, 0);
            }
            case GOTO -> {
                return new Syscall(operation, Reg.from(bytecode));
            }
            case INTERRUPT -> {
                // syscall.interruptOption = InterruptOption.fromBytecode(bytecode);
                // syscall.data = bytecode & 0xff;
                InterruptOption option = InterruptOption.fromBytecode(bytecode);
                if (option == InterruptOption.REGISTER) {
                    return new Syscall(operation, option, Reg.from(bytecode));
                }
                return new Syscall(operation, option, bytecode & 0xff);
            }
            case TRANSLATE -> {
                return new Syscall(operation, Reg.from(bytecode));
            }
        }
        return null;
    }

    @Override
    public int getBytecode() {
        return op.id | operation.id | (operation == Operation.INTERRUPT ? interruptOption.id : 0) | (data & 0xffff) | rg.code;
    }

    @Override
    public String toString() {
        switch(operation) {
            case FUNCTION -> { return String.format("SYSCALL 0x%04x", data); }
            case RETURN -> { return "SYSCALL RET"; }
            case GOTO -> { return String.format("SYSGOTO %s", MachineCode.translateReg(data)); }
            case INTERRUPT -> {
                return switch (interruptOption) {
                    case REGISTER -> String.format("INTERRUPT %s", MachineCode.translateReg(data));
                    case VALUE -> String.format("INTERRUPT %d", data);
                    case RETURN -> "INTERRUPT RET";
                };
            }
            case TRANSLATE -> { return String.format("TRANSLATE %s", MachineCode.translateReg(data)); }
        }
        return String.format("SYSCALL UNKNOWN (0x%s)", toHex(getBytecode()));
    }

    public enum Operation {
        FUNCTION(0x00),
        RETURN(0x01),
        GOTO(0x02),
        INTERRUPT(0x03),
        TRANSLATE(0x04)
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
            return byId.getOrDefault(bytecode & 0x00ff_0000, FUNCTION);
        }
    }

    public enum InterruptOption {
        REGISTER(0x00),
        VALUE(0x01),
        RETURN(0xff)
        ;

        public final int id;

        private InterruptOption(int id) {
            this.id = id << 8;
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
