package com.peter.emulator.lang;

public class ELStruct extends ELClass {

    public ELStruct(String name) {
        super(name);
    }

    public ELStruct(String name, Namespace namespace) {
        super(name, namespace);
    }

    @Override
    public String getClassType() {
        return "struct";
    }
}
