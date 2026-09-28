package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELValue;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.machinecode.Instruction;

public class ConstantValue {

    public final String value;
    public final int numValue;
    public final boolean isNum;

    public ConstantValue(int value) {
        this.value = Instruction.toHexLead(value);
        numValue = value;
        isNum = true;
    }
    public ConstantValue(int value, String name) {
        this.value = name;
        numValue = value;
        isNum = true;
    }

    public ConstantValue(String value) {
        this.value = value;
        numValue = 0;
        isNum = false;
    }
    
    public static ConstantValue constVar(ELVariable var) {
        return new ConstantValue(((ELValue.ELNumberValue) var.startingValue).value, var.getQualifiedName());
    }

    public static ConstantValue staticVar(ELVariable var) {
        return new ConstantValue("&" + var.getQualifiedName());
    }
    public static ConstantValue staticVar(String name) {
        return new ConstantValue("&"+name);
    }


    @Override
    public String toString() {
        return value;
    }
}
