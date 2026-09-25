package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Operator;

public class LiteralInstruction extends Instruction {

    public LiteralInstruction(int value) {
        super(Operator.UNKNOWN);
        this.data = value;
    }

    @Override
    public int getBytecode() {
        return data;
    }

    @Override
    public String getASM() {
        return toHexLead(data);
    }
}
