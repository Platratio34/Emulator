package com.peter.emulator.assembly.symbols;

import com.peter.emulator.assembly.Define;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Span;
import com.peter.emulator.machinecode.Instruction;

public class DefinitionSymbol extends ELSymbol {

    public final Define def;

    public DefinitionSymbol(Span span, Define def) {
        super(def.isAddress ? Type.VARIABLE_NAME : Type.VARIABLE_CONSTANT, span);
        this.def = def;
        definition = def.defSymbol;
    }

    @Override
    public String getText() {
        if (def.isAddress) {
            return String.format("Variable define `%s`", def.name);
        }
        return String.format("Define `%s`\n\nValue `%s`", def.name, Instruction.toHexLead(def.value));
    }

    @Override
    public boolean hasText() {
        return true;
    }

}
