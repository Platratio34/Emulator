package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.ELValue.ELNumberValue;
import com.peter.emulator.lang.ELValue.ELStringValue;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class ResolveAction extends ComplexAction {

    public final Register reg;
    public final ELType returnType;
    public final ELVariable returnVar;

    public ResolveAction(ActionScope scope, Register reg, ELVariable var, IdentifierToken id, boolean byValue) {
        super(scope);
        this.reg = reg;

        IdentifierToken it = id;
        if (!id.value.equals(var.name)) {
            if (id.subTokens == null)
                throw ELAnalysisError.fatal("Could not find identifier for provided variable", it);
            while (it != null) {
                if (it.value.equals(var.name))
                    break;
                scope.addSymbol(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, it.spanFirst(), "### `%s`", it.value));
                if (it.hasSub())
                    it = it.sub(0);
                else
                    throw ELAnalysisError.fatal("Could not find identifier for provided variable", it); 
                // index++;
                // if (index == id.subTokens.size() - 1)
                //     throw ELAnalysisError.fatal("Could not find identifier for provided variable", it);
                // it = id.sub(index - 1);
            }
        }

        switch (var.varType) {
            case CONST -> {
                switch (var.startingValue) {
                    case ELNumberValue nv -> {
                        actions.add(new DirectAction("LOAD %s %d", reg, nv.value));
                        returnType = var.type;
                        returnVar = var;
                        // scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.spanFirst(),
                        //         "### `const %s %s = %d`", var.type.typeString(), it.value, nv.value));
                        scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
                        return;
                    }
                    case ELStringValue sv -> {
                        if (sv.type.equals(ELPrimitives.CHAR)) {
                            actions.add(new DirectAction("LOAD %s '%s'", reg, sv.value));
                            returnType = var.type;
                            returnVar = var;
                            // scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.spanFirst(),
                            //         "### `const %s %s = '%s'`", var.type.typeString(), it.value, sv.value));
                            scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
                            return;
                        } else {
                            throw ELAnalysisError.error("Can not reference constant char* right now");
                        }
                    }
                    default -> throw ELAnalysisError.error("Unknown constant type");
                }
            }
            case STATIC -> actions.add(new DirectAction("LOAD %s &%s", reg, var.getQualifiedName()));
            case MEMBER -> actions.add(new DirectAction("COPY r0 %s\nINC %s %d", reg, reg, var.offset));
            case SCOPE -> {
                actions.add(new DirectAction("COPY r15 %s", reg));
                if (var.offset != 0)
                    actions.add(new DirectAction("INC %s %d", reg, var.offset));
            }
        }
        scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
        // scope.addSymbol(new ELSymbol(var.finalVal ? ELSymbol.Type.VARIABLE_FINAL : ELSymbol.Type.VARIABLE_NAME, it.spanFirst(), "### `%s %s`", var.typeString(), it.value));

        ELVariable v = var;
        ELType t = v.type;
        if(id.hasSub()) {
            while (it != null) {
                if (it.indexed()) {
                    if (!v.type.isIndexable())
                        throw ELAnalysisError.error(v.type.typeString() + " is not indexable",
                                it.index.subFirst().startLocation.span(it.index.subLast().endLocation));
                    Register r2 = scope.firstFree();
                    ExpressionAction indexExp = new ExpressionAction(scope, id.index.subTokens, r2);
                    if (indexExp.outType != null && !indexExp.outType.equals(ELPrimitives.UINT32))
                        throw ELAnalysisError.error("Index must resolve to a uint32",
                                id.index.subFirst().startLocation.span(id.index.subLast().endLocation));
                    int wds = v.sizeofWords();
                    if (wds > 1) {
                        Register r3 = scope.firstFree();
                        actions.add(new DirectAction("LOAD %s %d\nMUL %s %s %s", r3, v.sizeofWords(), r2, r2, r3));
                    }
                    actions.add(new DirectAction("ADD %s %s %s", reg, reg, r2));
                    actions.add(new DirectAction("LOAD MEM %s %s", reg, reg));
                    r2.release();
                    t = t.resolve();
                }
                
                scope.addSymbol(new ELVarSymbol(v, it.spanFirst()));
                // scope.addSymbol(new ELSymbol(v.finalVal ? ELSymbol.Type.VARIABLE_FINAL : ELSymbol.Type.VARIABLE_NAME, it.spanFirst(), "### `%s %s`", v.typeString(), v.name));
                
                if(it.hasSub())
                    it = it.sub(0);
                else
                    break;

                if (t.isPointer() || t.isAddress()) {
                    actions.add(new DirectAction("LOAD MEM %s %s", reg, reg));
                    t = t.resolve();
                }

                ELClass clazz = t.getELClass();
                if (clazz == null)
                    throw ELAnalysisError.fatal("Type was missing class (type was `" + t.typeString()+"`; "+t.toString()+")", it);
                if (!clazz.memberVariables.containsKey(it.value))
                    throw ELAnalysisError.fatal("Unknown member " + it.value + "in type" + clazz.getQualifiedName(), it);
                v = clazz.memberVariables.get(it.value);
                t = v.type;
            }
        }
        if (byValue)
            actions.add(new DirectAction("LOAD MEM %s %s", reg, reg));
        returnType = t;
        returnVar = v;
    }
}
