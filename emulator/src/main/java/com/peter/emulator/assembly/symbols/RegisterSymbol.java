package com.peter.emulator.assembly.symbols;

import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.symbols.ELSymbol;
import com.peter.emulator.lang.symbols.SymbolType;
import com.peter.emulator.machinecode.Reg;

public class RegisterSymbol extends ELSymbol {

    public final Reg reg;

    public RegisterSymbol(Span span, Reg reg) {
        super(SymbolType.PARAMETER, span);
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
