package com.peter.emulator.machinecode;

import java.util.function.BiFunction;

import com.peter.emulator.machinecode.Instruction.Generic;
import com.peter.emulator.machinecode.Instruction.Unknown;

public enum Operator {
    UNKNOWN(-1, null),

    NO_OP(0x00, Generic::fromBytecode),
    LOAD(0x01, Load::fromBytecode),
    STORE(0x02, StoreInstruction::fromBytecode),

    MATH(0x04, MathInstruction::fromBytecode),
    GOTO(0x05, Goto::fromBytecode),
    SET(0x06, SetInstruction::fromBytecode),

    STACK(0x10, StackInstruction::fromBytecode),
    SYSCALL(0x11, Syscall::fromBytecode),

    HALT(0xff, Generic::fromBytecode)
    ;

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

    protected static Operator[] byId;

    private void setup() {
        if (byId == null) {
            byId = new Operator[256];
            for(int i = 0; i < 256; i++) {
                byId[i] = UNKNOWN;
            }
        }
        byId[id >>> 24] = this;
    }

    public static Operator fromBytecode(int bytecode) {
        // System.out.println(toHexLead(bytecode & 0xff00_0000));
        return byId[(bytecode >>> 24) & 0xff];
    }
}