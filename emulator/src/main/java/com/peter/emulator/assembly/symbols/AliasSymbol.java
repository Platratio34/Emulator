package com.peter.emulator.assembly.symbols;

import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.symbols.ELSymbol;
import com.peter.emulator.lang.symbols.SymbolType;
import com.peter.emulator.machinecode.Reg;

public class AliasSymbol extends ELSymbol {

    public final Reg reg;

    public AliasSymbol(Span span, Reg reg) {
        super(SymbolType.PARAMETER, span);
        this.reg = reg;
    }

    public AliasSymbol(Span span, AliasSymbol definition) {
        super(SymbolType.PARAMETER, span);
        reg = definition.reg;
        this.definition = definition;
    }
    
    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        return String.format("Register alias for `%s`", reg);
    }

    public AliasSymbol use(Span lastSpan) {
        return new AliasSymbol(lastSpan, this);
    }

}
