package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.InlineType;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.ELFunction.FunctionType;

public class ELFuncCallSymbol extends ELSymbol {

    public final ELFunction func;

    public ELFuncCallSymbol(ELFunction func, Span span) {
        super(SymbolType.FUNCTION_NAME, span);
        this.func = func;
        this.definition = func.defSymbol;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        String out = "`";
        // if(func.finalVal)
        //     out += "final ";
        // else if (func.varType == ELVariable.Type.CONST)
        //     out += "const ";
        // out += String.format("%s %s`", func.typeString(), (func.varType == ELVariable.Type.SCOPE) ? func.name : func.getQualifiedName());
        // if ((func.finalVal || func.varType == ELVariable.Type.CONST) && func.hasValue())
        //     out += "\n\nValue: `" + func.getValueDebug() + "`";
        // if (func.type.getELClass() != null) {
        //     out += "\n\nBase Class: `" + func.type.getELClass().getQualifiedName() + "`";
        //     out += "\n(`" + func.type.toString() + "`)";
        // }
        out += func.protection.value + " ";
        if (func.type == FunctionType.STATIC)
            out += "static ";
        if (func.extern)
            out += "extern ";
        if (func.inline != InlineType.OUTLINE)
            out += "inline ";
        if (func.ret != null)
            out += func.ret.typeString() + " ";
        else
            out += "void ";
        switch (func.type) {
            case CONSTRUCTOR -> {
                out += func.namespace.getQualifiedName();
            }
            case DESTRUCTOR -> {
                out += func.namespace.namespace.getQualifiedName() + ".~" + func.namespace.cName;
            }
            case OPERATOR, INSTANCE, STATIC -> {
                out += func.getQualifiedName();
            }
        }
        out += "(";
        boolean f = true;
        for (String pName : func.paramOrder) {
            if (!f)
                out += ", ";
            ELType param = func.params.get(pName);
            out += String.format("%s %s", param.typeString(), pName);
            f = false;
        }
        out += ")`";
        if (func.doc != null) {
            out += "\n\n" + func.doc.getMD();
        }
        return out/* + "\n\n\n\n"+span.debugString()*/;
    }

}