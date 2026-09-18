package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.machinecode.Reg;

public class ResolveAction extends ComplexAction {

    public final Register reg;
    public final ELType returnType;
    public final ELVariable returnVar;
    public boolean wasConst = false;

    public ResolveAction(ActionScope scope, Register reg, ELVariable var, IdentifierToken id, boolean byValue) {
        this(scope, reg, var, id, byValue, false);
    }

    public ResolveAction(ActionScope scope, Register reg, ELVariable var, IdentifierToken id, boolean byValue, boolean dropLast) {
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
                if (it.hasSub()) {
                    it = it.sub(0);
                    if (dropLast && !it.hasSub()) {
                        it = null;
                    }
                } else
                    throw ELAnalysisError.fatal("Could not find identifier for provided variable", it); 
                // index++;
                // if (index == id.subTokens.size() - 1)
                //     throw ELAnalysisError.fatal("Could not find identifier for provided variable", it);
                // it = id.sub(index - 1);
            }
        }
        
        wasConst = false;
        Reg sourceReg = null;
        boolean regIsValue = false;
        String constAddr = null;
        if (var instanceof PseudoVariable psV) {
            sourceReg = psV.register;
            regIsValue = true;
        } else {
            switch (var.varType) {
                case CONST -> {
                    addDirect("LOAD %s %s", reg, var.getQualifiedName());
                    wasConst = true;
                }
                case STATIC -> {
                    // addDirect("LOAD %s &%s", reg, var.getQualifiedName());
                    constAddr = var.getQualifiedName();
                }
                case MEMBER -> {
                    if (var.offset != 0) {
                        if (var.offset > 0 && var.offset <= 255) {
                            addDirect("ADD %s r0 %d", reg, var.offset);
                        } else if (var.offset < 0 && var.offset >= -255) {
                            addDirect("SUB %s r0 %d", reg, -var.offset);
                        } else {
                            addDirect("COPY r0 %s", reg);
                            addDirect("INC %s %d", reg, var.offset);
                        }
                    } else {
                        sourceReg = Reg.R0;
                    }
                }
                case SCOPE -> {
                    if (var.offset != 0) {
                        if (var.offset > 0 && var.offset <= 255) {
                            addDirect("ADD %s r15 %d", reg, var.offset);
                        } else if (var.offset < 0 && var.offset >= -255) {
                            addDirect("SUB %s r15 %d", reg, -var.offset);
                        } else {
                            addDirect("COPY r15 %s", reg);
                            addDirect("INC %s %d", reg, var.offset);
                        }
                    } else {
                        sourceReg = Reg.R15;
                    }
                }
            }
        }
        // scope.addSymbol(new ELVarSymbol(var, it.spanFirst()));
        // scope.addSymbol(new ELSymbol(var.finalVal ? ELSymbol.Type.VARIABLE_FINAL : ELSymbol.Type.VARIABLE_NAME, it.spanFirst(), "### `%s %s`", var.typeString(), it.value));
        addReserve(reg);
        ELVariable v = var;
        ELType t = v.type;
        // if(id.hasSub()) {
            while (it != null) {
                if (it.indexed()) {
                    if (!v.type.isIndexable())
                        throw ELAnalysisError.error(v.type.typeString() + " is not indexable",
                                it.index.subFirst().startLocation.span(it.index.subLast().endLocation));
                    if (t.isPointer() && !wasConst && sourceReg == null) {
                        if (constAddr != null) {
                            addDirect("LOAD MEM %s &%s", reg, constAddr);
                            constAddr = null;
                        } else {
                            addDirect("LOAD MEM %s %s", reg, reg);
                        }
                    } else if (constAddr != null) {
                        addDirect("LOAD %s &%s", reg, constAddr);
                        constAddr = null;
                    }
                    Register rIndex = newRegister();
                    addReserve(rIndex);
                    // addDirect("// index; %s", rIndex);
                    Expression indexExp = new Expression(scope, it.index.subTokens, rIndex);
                    if (indexExp.getType() != null && !indexExp.getType().equals(ELPrimitives.INT32))
                        throw ELAnalysisError.errorF(it.index.subFirst().startLocation.span(it.index.subLast().endLocation), "Index must resolve to an int32, was a %s", indexExp.getType().typeString());
                    ELType resolvedType = t.resolve(it.span());
                    int size = resolvedType.sizeof();
                    if (indexExp.isConstant()) {
                        int amt = indexExp.getConstant() * size;
                        if (amt > 0) {
                            if (sourceReg != null) {
                                if (amt > 0 && amt <= 255) {
                                    addDirect("ADD %s %s %d", reg, sourceReg, amt);
                                } else if(amt < 0 && amt >= -255) {
                                    addDirect("SUB %s %s %d", reg, sourceReg, -amt);
                                } else {
                                    addDirect("COPY %s %s", sourceReg, reg);
                                    addDirect("INC %s %d", reg, amt);
                                }
                                sourceReg = null;
                            } else {
                                addDirect("INC %s %d", reg, amt);
                            }
                        }
                    } else {
                        actions.add(indexExp);
                        if (size > 1 && (size & (size - 1)) == 0) { // power of 2
                            addDirect("LSH %s %s %d", rIndex, rIndex, Integer.numberOfTrailingZeros(size));
                        } else if (size > 1) {
                            Register rSize = newRegister();
                            addFind(rSize);
                            addDirect("LOAD %s %d\nMUL %s %s %s", rSize, size, rIndex, rIndex, rSize);
                        }
                        if (sourceReg != null) {
                            addDirect("ADD %s %s %s", reg, sourceReg, rIndex);
                            sourceReg = null;
                        } else {
                            addDirect("ADD %s %s %s", reg, reg, rIndex);
                        }
                    }
                    addRelease(rIndex);
                    t = resolvedType;
                    wasConst = false;
                }
                
                scope.addSymbol(new ELVarSymbol(v, it.spanFirst()));
                
                if (it.hasSub()) {
                    it = it.sub(0);
                    if (dropLast && !it.hasSub()) {
                        break;
                    }
                } else
                    break;

                
                if (t.isPointer() || t.isAddress()) {
                    if (sourceReg != null) {
                        addDirect("LOAD MEM %s %s", reg, sourceReg);
                        sourceReg = null;
                    } else if (constAddr != null) {
                        addDirect("LOAD MEM %s &%s", reg, constAddr);
                        constAddr = null;
                    } else {
                        addDirect("LOAD MEM %s %s", reg, reg);
                    }
                    t = t.resolve(it.span());
                }
                wasConst = false;

                ELClass clazz = t.getELClass();
                if (clazz == null) {
                    scope.unit.errors.warning(String.format(
                            "Type `%s` was missing class, analyzing... (found resolving variable `%s`)", t.typeString(), var.debugString()), it);
                    t.analyze(scope.unit.errors, scope.namespace, scope.unit);
                    clazz = t.getELClass();
                }
                if (clazz == null)
                    throw ELAnalysisError.fatal("Type was missing class (type was `" + t.typeString()+"`; "+t.toString()+")", it);
                if (!clazz.memberVariables.containsKey(it.value))
                    throw ELAnalysisError.fatal("Unknown member " + it.value + " in type" + clazz.getQualifiedName(), it);
                v = clazz.memberVariables.get(it.value);
                if (v.offset != 0) {
                    if (sourceReg != null) {
                        if (v.offset > 0 && v.offset <= 255) {
                            addDirect("ADD %s %s %d", reg, sourceReg, v.offset);
                        } else if(v.offset < 0 && v.offset >= -255) {
                            addDirect("SUB %s %s %d", reg, sourceReg, -v.offset);
                        } else {
                            addDirect("COPY %s %s", sourceReg, reg);
                            addDirect("INC %s %d", reg, v.offset);
                        }
                    } else if (constAddr != null) {
                        addDirect("LOAD %s &%s", reg, constAddr);
                        constAddr = null;
                        addDirect("INC %s %d", reg, v.offset);
                    } else {
                        addDirect("INC %s %d", reg, v.offset);
                    }
                }
                t = v.type;
            }

        // }
        String size = switch (t.sizeof()) {
            case 2 -> " SHORT";
            case 1 -> " BYTE";
            default -> "";
        };
        
        if (byValue && !wasConst) {
            if (sourceReg != null) {
                if (regIsValue) {
                    addDirect("COPY %s %s", sourceReg, reg);
                } else {
                    addDirect("LOAD MEM%s %s %s", size, reg, sourceReg);
                }
            } else if (constAddr != null) {
                addDirect("LOAD MEM%s %s &%s", size, reg, constAddr);
                constAddr = null;
            }else {
                addDirect("LOAD MEM%s %s %s", size, reg, reg);
            }
        }
        if (sourceReg != null && !byValue && regIsValue) {
            throw ELAnalysisError.errorF("Can not get register based variable %s by address", it.value, it.span());
        }
        if (constAddr != null) {
            addDirect("LOAD %s &%s", reg, constAddr);
            constAddr = null;
        }
        returnType = t;
        returnVar = v;
    }
}
