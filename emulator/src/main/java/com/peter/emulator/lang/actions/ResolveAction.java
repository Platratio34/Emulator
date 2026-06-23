package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
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
                if (it.value.equals("this"))
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_FINAL, it.spanFirst(), "### `%s* this`", var.namespace.getQualifiedName()));
                else
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
                addDirect("LOAD %s %s", reg, var.getQualifiedName());
                // switch (var.startingValue) {
                //     case ELNumberValue nv -> {
                //         addDirect("LOAD %s %d", reg, nv.value));
                //         returnType = var.type;
                //         returnVar = var;
                //         // scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.spanFirst(),
                //         //         "### `const %s %s = %d`", var.type.typeString(), it.value, nv.value));
                //         scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
                //         return;
                //     }
                //     case ELStringValue sv -> {
                //         if (sv.type.equals(ELPrimitives.CHAR)) {
                //             addDirect("LOAD %s '%s'", reg, sv.value));
                //             returnType = var.type;
                //             returnVar = var;
                //             // scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.spanFirst(),
                //             //         "### `const %s %s = '%s'`", var.type.typeString(), it.value, sv.value));
                //             scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
                //             return;
                //         } else {
                //             throw ELAnalysisError.error("Can not reference constant char* right now");
                //         }
                //     }
                //     default -> throw ELAnalysisError.error("Unknown constant type");
                // }
            }
            case STATIC -> addDirect("LOAD %s &%s", reg, var.getQualifiedName());
            case MEMBER -> {
                addDirect("COPY r0 %s", reg);
                if (var.offset != 0)
                    addDirect("INC %s %d", reg, var.offset);
            }
            case SCOPE -> {
                addDirect("COPY r15 %s", reg);
                if (var.offset != 0)
                    addDirect("INC %s %d", reg, var.offset);
            }
        }
        scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
        // scope.addSymbol(new ELSymbol(var.finalVal ? ELSymbol.Type.VARIABLE_FINAL : ELSymbol.Type.VARIABLE_NAME, it.spanFirst(), "### `%s %s`", var.typeString(), it.value));
        reg.reserve();
        ELVariable v = var;
        ELType t = v.type;
        // if(id.hasSub()) {
            while (it != null) {
                if (it.indexed()) {
                    if (!v.type.isIndexable())
                        throw ELAnalysisError.error(v.type.typeString() + " is not indexable",
                                it.index.subFirst().startLocation.span(it.index.subLast().endLocation));
                    
                    Register rIndex = scope.firstFree();
                    // addDirect("// index; %s", rIndex);
                    ExpressionAction indexExp = new ExpressionAction(scope, id.index.subTokens, rIndex);
                    if (indexExp.outType != null && !indexExp.outType.equals(ELPrimitives.UINT32))
                        throw ELAnalysisError.error("Index must resolve to a uint32",
                                id.index.subFirst().startLocation.span(id.index.subLast().endLocation));
                    int size = t.resolve(it.span()).sizeof();
                    // if(t.isPointer())
                    //     addDirect("LOAD MEM %s %s", reg, reg);
                    if (indexExp.wasConst) {
                        addDirect("INC %s %d", reg, indexExp.constValue * size);
                    } else {
                        actions.add(indexExp);
                        if (size > 1) {
                            Register rSize = scope.firstFree();
                            addDirect("LOAD %s %d\nMUL %s %s %s", rSize, size, rIndex, rIndex, rSize);
                            rSize.release();
                        }
                        addDirect("ADD %s %s %s", reg, reg, rIndex);
                    }
                    rIndex.release();
                    t = t.resolve(it.span());
                }
                
                scope.addSymbol(new ELVarSymbol(v, it.spanFirst()));
                // scope.addSymbol(new ELSymbol(v.finalVal ? ELSymbol.Type.VARIABLE_FINAL : ELSymbol.Type.VARIABLE_NAME, it.spanFirst(), "### `%s %s`", v.typeString(), v.name));
                
                if(it.hasSub())
                    it = it.sub(0);
                else
                    break;

                if (t.isPointer() || t.isAddress()) {
                    addDirect("LOAD MEM %s %s", reg, reg);
                    t = t.resolve(it.span());
                }

                ELClass clazz = t.getELClass();
                if (clazz == null)
                    throw ELAnalysisError.fatal("Type was missing class (type was `" + t.typeString()+"`; "+t.toString()+")", it);
                if (!clazz.memberVariables.containsKey(it.value))
                    throw ELAnalysisError.fatal("Unknown member " + it.value + "in type" + clazz.getQualifiedName(), it);
                v = clazz.memberVariables.get(it.value);
                t = v.type;
            }

        // }
        String size = switch (t.sizeof()) {
            case 2 -> " SHORT";
            case 1 -> " BYTE";
            default -> "";
        };
        if (byValue && v.varType != ELVariable.Type.CONST)
            addDirect("LOAD MEM%s %s %s", size, reg, reg);
        returnType = t;
        returnVar = v;
    }
}
