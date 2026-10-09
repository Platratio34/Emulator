package com.peter.emulator.assembly.symbols;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Span;
import com.peter.emulator.machinecode.Reg;

public class RegisterSymbol extends ELSymbol {

    public final Reg reg;

    public RegisterSymbol(Span span, Reg reg) {
        super(Type.PARAMETER, span);
        this.reg = reg;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override 
    public String getText() {
        return reg.description;
    }

}
