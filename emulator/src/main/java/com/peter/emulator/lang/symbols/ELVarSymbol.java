package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.Span;

public class ELVarSymbol extends ELSymbol {

    public final ELVariable var;

    public ELVarSymbol(ELVariable var, Span span) {
        super((var.finalVal ? Type.VARIABLE_FINAL : Type.VARIABLE_NAME), span);
        this.var = var;
        this.definition = var.defSymbol;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        String out = "`";
        if(var.finalVal)
            out += "final ";
        else if (var.varType == ELVariable.Type.CONST)
            out += "const ";
        out += String.format("%s %s`", var.typeString(), (var.varType == ELVariable.Type.SCOPE) ? var.name : var.getQualifiedName());
        if ((var.finalVal || var.varType == ELVariable.Type.CONST) && var.hasValue())
            out += "\n\nValue: `" + var.getValueDebug() + "`";
        // if (var.type.getELClass() != null) {
        //     out += "\n\nBase Class: `" + var.type.getELClass().getQualifiedName() + "`";
        //     out += "\n(`" + var.type.toString() + "`)";
        // }
        if (var.varType == ELVariable.Type.MEMBER) {
            out += "\n\nOffset: " + var.offset;
        }
        if (var.doc != null) {
            out += "\n\n" + var.doc.desc;
        }
        return out/* + "\n\n\n\n"+span.debugString()*/;
    }

}