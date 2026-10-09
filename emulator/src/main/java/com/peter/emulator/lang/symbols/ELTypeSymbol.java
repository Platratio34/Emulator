package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELType;

public class ELTypeSymbol extends ELSymbol {

    public final ELType elType;
    public final boolean operator;

    public ELTypeSymbol(ELType type) {
        super(SymbolType.CLASS_NAME, type.span());
        elType = type;
        operator = false;
        ELClass clazz = type.getELClass();
        if(clazz != null)
            definition = clazz.defSymbol;
    }

    public ELTypeSymbol(ELType type, boolean operator) {
        super(operator ? SymbolType.OPERATOR : SymbolType.CLASS_NAME, type.baseRef().nameSpan);
        elType = type;
        this.operator = operator;
        if (!operator) {
            ELClass clazz = type.getELClass();
            if (clazz != null)
                definition = clazz.defSymbol;
        }
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        if (elType.isVoid())
            return String.format("`void`");
        String out = String.format("`%s` (%d bytes)", elType.typeString(), elType.sizeof());
        ELClass clazz = elType.getELClass();
        if (clazz != null) {
            if (clazz.docComment != null) {
                out += "\n\n" + clazz.docComment.getMD();
            }
        }
        return out;
    }
}