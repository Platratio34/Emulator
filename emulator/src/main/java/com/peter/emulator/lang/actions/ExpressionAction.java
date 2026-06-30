package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELSymbol.Modifier;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.lang.ELValue.ELNumberValue;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.*;

public class ExpressionAction extends ComplexAction {

    public final Register targetReg;
    public final ELType outType;
    public final boolean wasConst;
    public final int constValue;

    protected static int nextStr = 0;
    
    public ExpressionAction(ActionScope scope, ArrayList<Token> tokens, Register targetReg) {
        super(scope);
        this.targetReg = targetReg;
        int wI = 0;

        OperatorToken.Type lastOp = null;
        ELType lastType = null;

        boolean addressOf = false;
        int resolvePointer = 0;
        boolean not = false;

        boolean _wasConst = false;
        int _constValue = 0;

        while (wI < tokens.size()) {
            Token tkn = tokens.get(wI);
            switch (tkn) {
                case OperatorToken ot -> {
                    switch (ot.type) {
                        case ADD, SUB, BITWISE_OR, BITWISE_NOR, LEFT_SHIFT, RIGHT_SHIFT, AND, OR, LEQ, GEQ, ANGLE_LEFT,
                                ANGLE_RIGHT, NEQ, EQ2 -> {
                            if (lastOp != null)
                                throw ELAnalysisError.error(
                                        "Unknown or unsupported operation: " + ot.type + " (op, after " + lastOp + ")",
                                        tkn);
                            if (lastType == null)
                                throw ELAnalysisError.error(
                                        "Unknown or unsupported operation: " + ot.type
                                                + " (op at beginning of expression)",
                                        tkn);
                            lastOp = ot.type;
                        }
                        case BITWISE_AND -> {
                            if (lastOp != null || lastType == null) {
                                if (addressOf)
                                    throw ELAnalysisError.error("Can not get address of address of", tkn);
                                if (resolvePointer > 0)
                                    throw ELAnalysisError.error("Can not get address of pointer resolve", tkn);
                                addressOf = true;
                            } else {
                                lastOp = ot.type;
                            }
                        }

                        case POINTER -> {
                            if (lastOp != null || lastType == null) {
                                if (addressOf)
                                    throw ELAnalysisError.error("Can not do pointer resolve after address of", tkn);
                                resolvePointer++;
                            } else {
                                lastOp = ot.type;
                            }
                        }
                        case NOT -> {
                            if (lastOp != null || lastType == null) {
                                if (not)
                                    throw ELAnalysisError.error("Can not have more that one `!` in a row", tkn);
                                not = true;
                            } else {
                                throw ELAnalysisError.error("Unknown or unsupported operation: " + ot.type + " (op)",
                                        tkn);
                            }
                        }

                        default -> {
                            throw ELAnalysisError.error("Unknown or unsupported operation: " + ot.type + " (op)", tkn);
                        }
                    }
                }
                case NumberToken nt -> {
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.NUMERIC_LITERAL, nt.span(), "numeric literal\n`%d`",
                            nt.numValue));
                    if (addressOf || resolvePointer > 0)
                        throw ELAnalysisError.error("Invalid pointer operation", tkn);
                    if (not)
                        throw ELAnalysisError.error("Can't not a number literal", tkn);
                    Register tR = (lastType == null) ? targetReg : scope.firstFree();
                    int val = nt.numValue;
                    if (lastType != null) {
                        if(lastType.isPointer()) {
                            val *= lastType.stepSize();
                        } else if (!ELPrimitives.UINT32.canCastTo(lastType))
                            throw ELAnalysisError.error("Invalid type-cast (uint32 -> " + lastType.typeString() + ")",
                                    tkn);
                    } else {
                        lastType = ELPrimitives.UINT32;
                    }
                    if (lastOp != null) {
                        if (_wasConst) {
                            _constValue = applyConstValue(_constValue, val, lastOp);
                        }
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("INC %s %d", targetReg, val));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("INC %s %d", targetReg, -val));
                            }
                            case POINTER -> {
                                actions.add(new DirectAction("LOAD %s %d", tR, val));
                                actions.add(new DirectAction("MUL %s %s %s", targetReg, targetReg, tR));
                            }

                            case BITWISE_AND -> {
                                actions.add(new DirectAction("LOAD %s %d", tR, val));
                                actions.add(new DirectAction("AND %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("LOAD %s %d", tR, val));
                                actions.add(new DirectAction("OR %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("LOAD %s %d", tR, val));
                                actions.add(new DirectAction("NOR %s %s %s", targetReg, targetReg, tR));
                            }

                            case LEFT_SHIFT -> {
                                actions.add(new DirectAction("LSH %s %s %d", targetReg, targetReg, val));
                            }
                            case RIGHT_SHIFT -> {
                                actions.add(new DirectAction("RSH %s %s %d", targetReg, targetReg, val));
                            }

                            case EQ2 -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE EQ %s %s", targetReg, targetReg));
                            }
                            case NEQ -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE NEQ %s %s", targetReg, targetReg));
                            }
                            case LEQ -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE LEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_RIGHT -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE GT %s %s", targetReg, targetReg));
                            }

                            case GEQ -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE GEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_LEFT -> {
                                if (val != 0)
                                    actions.add(new DirectAction("INC %s %d", targetReg, -val));
                                actions.add(new DirectAction("SET FORCE LT %s %s", targetReg, targetReg));
                            }

                            default -> {
                                throw ELAnalysisError
                                        .error("Unknown or unsupported operation: " + lastOp + " (num lit)", tkn);
                            }
                        }
                    } else {
                        _wasConst = true;
                        _constValue = val;
                        actions.add(new DirectAction("LOAD %s %d", tR, val));
                        tR.reserve();
                    }
                    lastOp = null;
                }
                case IdentifierToken it -> {
                    Register tR = (lastType == null) ? targetReg : scope.firstFree();
                    ELType t;
                    switch (it.value) {
                        case "true", "false" -> {
                            _wasConst = false;
                            scope.addSymbol(
                                    new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.span(), "### Boolean literal"));
                            actions.add(new DirectAction("LOAD %s %d", tR, it.value.equals("true") ? 1 : 0));
                            t = ELPrimitives.BOOL;
                        }

                        case "nullptr" -> {
                            if (lastType == null) {
                                _wasConst = true;
                                _constValue = 0;
                            } else if (_wasConst) {
                                _constValue = applyConstValue(_constValue, 0, lastOp);
                            }
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.span(),
                                    "### Null pointer literal")).withModifiers(Modifier.LANGUAGE);
                            actions.add(new DirectAction("LOAD %s 0", tR));
                            t = ELPrimitives.VOID_PTR;
                        }

                        case "SysD" -> {
                            _wasConst = false;
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, it.spanFirst(),
                                    "### `SysD`\nSystem Direct Low-level module"));
                            if (!it.hasSub() || it.subTokens.size() != 1)
                                throw ELAnalysisError.error("Unable to resolve variable `" + it.debugString() + "`",
                                        it);
                            String vN = it.sub(0).value;
                            switch (vN) {
                                case "rPM", "rPMI" -> {
                                    actions.add(new DirectAction("COPY rPM %s", tR));
                                    t = ELPrimitives.BOOL;
                                }
                                case "rStack", "rMemTbl", "rStackI", "rMemTblI" -> {
                                    actions.add(new DirectAction("COPY %s %s", vN, tR));
                                    t = ELPrimitives.VOID_PTR;
                                }
                                default -> {
                                    actions.add(new DirectAction("COPY %s %s", vN, tR));
                                    t = ELPrimitives.UINT32;
                                }
                            }
                            if (vN.startsWith("r"))
                                scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_NAME, it.sub(0).span(),
                                        "### `%s %s`\nCPU register `%s`\n\n" + MachineCode.regDesc(vN), t.typeString(),
                                        vN, vN)).withModifiers(Modifier.LANGUAGE);
                        }
                        case "new" -> {
                            scope.addSymbol(Type.KEYWORD, tkn.span());
                            if(wI > 0 || tokens.size() > 2) {
                                throw ELAnalysisError.error("`new` expression must be only element of expression", tkn.span());
                            } else if(tokens.size() == 1) {
                                throw ELAnalysisError.error("Unexpected end of expression. Expected type for `new` expression", tkn.endLocation.span());
                            }
                            wI++;
                            tkn = tokens.get(wI);
                            if(tkn instanceof IdentifierToken it2) {
                                NewAction na = new NewAction(scope, it2, tR);
                                actions.add(na);
                                t = na.retType;
                                _wasConst = false;
                            } else {
                                throw ELAnalysisError.error("Unexpected token in `new` expression. Expected type, found "+tkn.debugString(), tkn.span());
                            }
                        }

                        default -> {
                            if (it.hasParams()) {
                                FunctionAction fA = new FunctionAction(scope, tR, it);
                                actions.add(fA);
                                t = fA.retType;
                                _wasConst = false;
                            } else {
                                ResolveAction rA = scope.loadVar(it, tR, !addressOf);
                                if (rA == null)
                                    throw ELAnalysisError.error("Unable to resolve variable `" + it.debugString() + "`",
                                            it);
                                actions.add(rA);
                                t = rA.returnType;
                                if (t.isConstant() && t.canCastTo(ELPrimitives.UINT32) && (_wasConst || lastOp == null)) {
                                    _wasConst = true;
                                    _constValue = applyConstValue(_constValue, ((ELNumberValue)(rA.returnVar.startingValue)).value, lastOp);
                                } else {
                                    _wasConst = false;
                                }
                            }
                        }
                    }
                    if (addressOf) {
                        if (t.isConstant())
                            throw ELAnalysisError
                                    .error("Can not get address of constant value (was " + t.typeString() + ")", it);
                        t = t.addressOf();
                    }
                    while (resolvePointer > 0) {
                        
                        _wasConst = false;
                        if (!t.isResolvable())
                            throw ELAnalysisError.error(
                                    "Unable to resolve non-pointer, address, or array (was " + t.typeString() + ")",
                                    it);
                        if (!t.isVoidPtr())
                            t = t.resolve(it.span());
                        else
                            t = null;
                        String size;
                        if (t != null) {
                            size = switch (t.sizeof()) {
                                case 1 -> " BYTE";
                                case 2 -> " SHORT";
                                default -> "";
                            };
                        } else {
                            size = "";
                        }
                        actions.add(new DirectAction("LOAD MEM%s %s %s", size, tR, tR));
                        resolvePointer--;
                    }
                    if (lastType != null) {
                        if (t != null && !t.canCastTo(lastType))
                            throw ELAnalysisError.error(
                                    "Invalid type-cast (" + t.typeString() + " -> " + lastType.typeString() + ")", tkn);
                    } else {
                        lastType = t;
                    }
                    // actions.add(new ResolveAction(scope, tR, it));
                    if (lastOp != null) {
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("ADD %s %s %s", targetReg, targetReg, tR));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                            }
                            case POINTER -> {
                                actions.add(new DirectAction("MUL %s %s %s", targetReg, targetReg, tR));
                            }

                            case BITWISE_AND -> {
                                actions.add(new DirectAction("AND %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("OR %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("NOR %s %s %s", targetReg, targetReg, tR));
                            }

                            case LEFT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }
                            case RIGHT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }

                            case EQ2 -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE EQ %s %s", targetReg, targetReg));
                            }
                            case NEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE NEQ %s %s", targetReg, targetReg));
                            }
                            case LEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE LEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_RIGHT -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE GT %s %s", targetReg, targetReg));
                            }

                            case GEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE GEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_LEFT -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE LT %s %s", targetReg, targetReg));
                            }

                            default -> {
                                throw ELAnalysisError.error("Unknown or unsupported operation: " + lastOp + " (var)",
                                        tkn);
                            }
                        }
                        tR.release();
                    } else {
                        tR.reserve();
                    }
                    addressOf = false;
                    not = false;
                    lastOp = null;
                }
                case SetToken st -> {
                    if (addressOf)
                        throw ELAnalysisError.error("Can not get address of an expression", tkn);
                    // but what if this is casting?
                    Register tR = (lastType == null) ? targetReg : scope.firstFree();
                    if (st.subTokens.isEmpty())
                        throw ELAnalysisError.error("Empty expression", st);
                    ExpressionAction expA = new ExpressionAction(scope, st.subTokens, tR);
                    actions.add(expA);
                    ELType t = expA.outType;
                    while (resolvePointer > 0) {
                        _wasConst = false;
                        if (t == null || !t.isResolvable())
                            throw ELAnalysisError.error(
                                    "Unable to resolve non-pointer, address, or array (was " + t.typeString() + ")",
                                    st);
                        String size = switch (t.sizeof()) {
                            case 1 -> " BYTE";
                            case 2 -> " SHORT";
                            default -> "";
                        };
                        actions.add(new DirectAction("LOAD MEM%s %s %s", size, tR, tR));
                        resolvePointer--;
                        if (t != null && !t.isVoidPtr())
                            t = t.resolve(st.span());
                        else
                            t = null;
                    }
                    if (lastType != null) {
                        if (t != null && !t.canCastTo(lastType))
                            throw ELAnalysisError.error(
                                    "Invalid type-cast (" + t.typeString() + " -> " + lastType.typeString() + ")", tkn);
                        if (expA.wasConst && resolvePointer == 0 && _wasConst) {
                            _constValue = applyConstValue(_constValue, expA.constValue, lastOp);
                        } else if (_wasConst) {
                            _wasConst = false;
                        }
                    } else {
                        lastType = t;
                        if (expA.wasConst && resolvePointer == 0) {
                            _constValue = expA.constValue;
                            _wasConst = true;
                        }
                    }
                    if (lastOp != null) {
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("ADD %s %s %s", targetReg, targetReg, tR));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_AND -> {
                                actions.add(new DirectAction("AND %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("OR %s %s %s", targetReg, targetReg, tR));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("NOR %s %s %s", targetReg, targetReg, tR));
                            }

                            case LEFT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }
                            case RIGHT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }

                            case EQ2 -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE EQ %s %s", targetReg, targetReg));
                            }
                            case NEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE NEQ %s %s", targetReg, targetReg));
                            }
                            case LEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE LEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_RIGHT -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE GT %s %s", targetReg, targetReg));
                            }

                            case GEQ -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE GEQ %s %s", targetReg, targetReg));
                            }
                            case ANGLE_LEFT -> {
                                actions.add(new DirectAction("SUB %s %s %s", targetReg, targetReg, tR));
                                actions.add(new DirectAction("SET FORCE LT %s %s", targetReg, targetReg));
                            }

                            default -> {
                                throw ELAnalysisError.error("Unknown or unsupported operation: " + lastOp + " (exp)",
                                        tkn);
                            }
                        }
                        tR.release();
                    } else {
                        tR.reserve();
                    }
                    not = false;
                    lastOp = null;
                }
                case StringToken st -> {
                    _wasConst = false;
                    if (addressOf)
                        throw ELAnalysisError.error("Can not get address of a string literal", tkn);
                    if (wI > 0 || tokens.size() > 1)
                        throw ELAnalysisError.error("String literal must be only element of expression", tkn);
                    if (st.ch) {
                        addDirect("LOAD %s '%s'", targetReg, st.escapedValue());
                        lastType = ELPrimitives.CHAR;
                    } else {
                        String id = "exp_str_" + (nextStr++);
                        addDirect("#define %s \"%s\"", id, st.escapedValue());
                        addDirect("LOAD %s %s", targetReg, id);
                        lastType = ELPrimitives.CHAR.pointerTo();
                    }

                    // Register tR = (lastType == null) ? targetReg : scope.firstFree();
                }
                default -> {
                    throw ELAnalysisError.error("Unexpected token found in expression: " + tkn, tkn);
                }
            }
            wI++;
        }
        // if (lastType == null)
        //     throw ELAnalysisError.error("Un-typed expression", tokens.getFirst().startLocation.span(tokens.getLast().endLocation));
        outType = lastType;
        if (_wasConst) {
            wasConst = true;
            // wasConst = false;
            constValue = _constValue;
            actions.clear();
            addDirect("LOAD %s %d", targetReg, constValue);
        } else {
            wasConst = false;
            constValue = 0;
        }
    }

    public int applyConstValue(int lVal, int nVal, OperatorToken.Type op) {
        if (op == null)
            return nVal;
        switch (op) {
            case ADD -> {
                lVal += nVal;
            }
            case SUB -> {
                lVal -= nVal;
            }
            case POINTER -> {
                lVal *= nVal;
            }

            case BITWISE_AND -> {
                lVal &= nVal;
            }
            case BITWISE_OR -> {
                lVal |= nVal;
            }
            case BITWISE_NOR -> {
                lVal ^= nVal;
            }

            case LEFT_SHIFT -> {
                lVal = lVal << nVal;
            }
            case RIGHT_SHIFT -> {
                lVal = lVal >> nVal;
            }

            case EQ2 -> {
                lVal = (lVal == nVal) ? 1 : 0;
            }
            case NEQ -> {
                lVal = (lVal != nVal) ? 1 : 0;
            }
            case LEQ -> {
                lVal = (lVal <= nVal) ? 1 : 0;
            }
            case ANGLE_LEFT -> {
                lVal = (lVal < nVal) ? 1 : 0;
            }

            case GEQ -> {
                lVal = (lVal >= nVal) ? 1 : 0;
            }
            case ANGLE_RIGHT -> {
                lVal = (lVal > nVal) ? 1 : 0;
            }
            default -> {
            }
        }
        return lVal;
    }

    /*
    var1 = var2 + 1;
    
    ->
    EXPRESSION r1 var2 + 1
        RESOLVE r1 var2 BY VALUE
        INC r1 1
    RESOLVE r2 var1
    MEM STORE r1 r2
    
    ----
    
    var1 = var2 + (var3 * 3);
    
    ->
    EXPRESSION r1 var2 + (var3 * 3)
        RESOLVE r1 var2 BY VALUE
        EXPRESSION r2 var3 * 3
            RESOLVE r2 var3 BY VALUE
            LOAD r3 [3]
            MUL r2 r2 r3
        INC r1 r2
    RESOLVE r2 var1
    MEM STORE r1 r2
    
    ----
    
    var1 = (var2 + 3) * (var3 * 3);
    
    ->
    EXPRESSION r1 (var2 + 3) * (var3 * 3)
        EXPRESSION r1 var2 + 3
            RESOLVE r1 var2 BY VALUE
            INC r1 [3]
        EXPRESSION r2 var3 * 3
            RESOLVE r2 var3 BY VALUE
            LOAD r3 [3]
            MUL r2 r2 r3
        MUL r1 r1 r2
    RESOLVE r2 var1
    MEM STORE r1 r2
    
     */
}
