package com.peter.emulator.lang;

public class ELStruct extends ELClass {

    public ELStruct(String name, Namespace namespace, ProgramUnit unit) {
        super(name, namespace, unit);
    }

    @Override
    public String getClassType() {
        return "struct";
    }
}
