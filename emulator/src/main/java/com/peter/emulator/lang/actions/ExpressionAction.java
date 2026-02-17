package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.NumberToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.SetToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ExpressionAction extends ComplexAction {

    public final int targetReg;
    public final ELType outType;
    
    public ExpressionAction(ActionScope scope, ArrayList<Token> tokens, int targetReg) {
        super(scope);
        this.targetReg = targetReg;
        String tRegStr = MachineCode.translateReg(targetReg);
        int wI = 0;

        OperatorToken.Type lastOp = null;
        ELType lastType = null;

        boolean addressOf = false;
        int resolvePointer = 0;
        boolean not = false;

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
                            lastOp = ot.type;
                        }
                        case BITWISE_AND -> {
                            if (lastOp != null) {
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
                            if (lastOp != null) {
                                if(addressOf)
                                    throw ELAnalysisError.error("Can not do pointer resolve after address of", tkn);
                                resolvePointer++;
                            } else {
                                lastOp = ot.type;
                            }
                        }
                        case NOT -> {
                            if (lastOp != null) {
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
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.NUMERIC_LITERAL, nt.span(), "numeric literal\n`%d`", nt.numValue));
                    if (addressOf || resolvePointer > 0)
                        throw ELAnalysisError.error("Invalid pointer operation", tkn);
                    if (not)
                        throw ELAnalysisError.error("Can't not a number literal", tkn);
                    if (lastType != null) {
                        if (!ELPrimitives.UINT32.canCastTo(lastType))
                            throw ELAnalysisError.error("Invalid type-cast (uint32 -> " + lastType.typeString() + ")",
                                    tkn);
                    } else {
                        lastType = ELPrimitives.UINT32;
                    }
                    int tR = (lastType == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    if (lastOp != null) {
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("INC %s %d", tRegStr, nt.numValue));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("DEC %s %d", tRegStr, nt.numValue));
                            }
                            case POINTER -> {
                                actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                                actions.add(new DirectAction("MUL %s %s %s", tRegStr, tRegStr, str));
                            }

                            case BITWISE_AND -> {
                                actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                                actions.add(new DirectAction("AND %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                                actions.add(new DirectAction("OR %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                                actions.add(new DirectAction("NOR %s %s %s", tRegStr, tRegStr, str));
                            }

                            case LEFT_SHIFT -> {
                                actions.add(new DirectAction("LSH %s %d", tRegStr, nt.numValue));
                            }
                            case RIGHT_SHIFT -> {
                                actions.add(new DirectAction("RSH %s %d", tRegStr, nt.numValue));
                            }

                            default -> {
                                throw ELAnalysisError
                                        .error("Unknown or unsupported operation: " + lastOp + " (num lit)", tkn);
                            }
                        }
                    } else {
                        actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                        scope.reserve(tR);
                    }
                    lastOp = null;
                }
                case IdentifierToken it -> {
                    int tR = (lastType == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    ELType t;
                    switch (it.value) {
                        case "true", "false" -> {
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.span(), "### Boolean literal"));
                            actions.add(new DirectAction("LOAD %s %d", str, it.value.equals("true") ? 1 : 0));
                            t = ELPrimitives.BOOL;
                        }

                        case "nullptr" -> {
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_CONSTANT, it.span(), "### Null pointer literal"));
                            actions.add(new DirectAction("LOAD %s 0", str));
                            t = ELPrimitives.VOID_PTR;
                        }

                        case "SysD" -> {
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, it.spanFirst(), "### `SysD`\nSystem Direct Low-level module"));
                            if(!it.hasSub() || it.subTokens.size() != 1)
                                throw ELAnalysisError.error("Unable to resolve variable", it);
                            String vN = it.sub(0).value;
                            if(vN.startsWith("r"))
                                scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_NAME, it.sub(0).span(), "### `%s`\nCPU register `%s`", vN, vN));
                            switch(vN) {
                                case "rPM" -> {
                                    actions.add(new DirectAction("COPY rPM %s", str));
                                    t = ELPrimitives.BOOL;
                                }
                                case "rStack", "rMemTbl" -> {
                                    actions.add(new DirectAction("COPY %s %s", vN, str));
                                    t = ELPrimitives.VOID_PTR;
                                }
                                default -> {
                                    actions.add(new DirectAction("COPY %s %s", vN, str));
                                    t = ELPrimitives.UINT32;
                                }
                            }
                        }

                        default -> {
                            ResolveAction rA = scope.loadVar(it, tR, addressOf);
                            if (rA == null)
                                throw ELAnalysisError.error("Unable to resolve variable", it);
                            actions.add(rA);
                            t = rA.returnType;
                        }
                    }
                    if(addressOf) {
                        if(!(t.isPointer() || t.isArray()))
                            throw ELAnalysisError.error("Can not get address of non-pointer or array (was "+t.typeString()+")", it);
                        t = t.addressOf();
                    }
                    while (resolvePointer > 0) {
                        if (!t.isResolvable())
                            throw ELAnalysisError.error(
                                    "Unable to resolve non-pointer, address, or array (was " + t.typeString() + ")",
                                    it);
                        actions.add(new DirectAction("LOAD MEM %s %s", str, str));
                        resolvePointer--;
                        t = t.resolve();
                    }
                    if (lastType != null) {
                        if (!t.canCastTo(lastType))
                            throw ELAnalysisError.error(
                                    "Invalid type-cast (" + t.typeString() + " -> " + lastType.typeString() + ")", tkn);
                    } else {
                        lastType = t;
                    }
                    // actions.add(new ResolveAction(scope, tR, it));
                    if (lastOp != null) {
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("ADD %s %s %s", tRegStr, tRegStr, str));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("SUB %s %s %s", tRegStr, tRegStr, str));
                            }
                            case POINTER -> {
                                actions.add(new DirectAction("MUL %s %s %s", tRegStr, tRegStr, str));
                            }

                            case BITWISE_AND -> {
                                actions.add(new DirectAction("AND %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("OR %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("NOR %s %s %s", tRegStr, tRegStr, str));
                            }

                            case LEFT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }
                            case RIGHT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }

                            default -> {
                                throw ELAnalysisError.error("Unknown or unsupported operation: " + lastOp + " (var)",
                                        tkn);
                            }
                        }
                        scope.release(tR);
                    } else {
                        scope.reserve(tR);
                    }
                    addressOf = false;
                    not = false;
                    lastOp = null;
                }
                case SetToken st -> {
                    if (addressOf)
                        throw ELAnalysisError.error("Can not get address of an expression", tkn);
                    // but what if this is casting?
                    int tR = (lastType == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    if(st.subTokens.isEmpty())
                        throw ELAnalysisError.error("Empty expression", st);
                    ExpressionAction expA = new ExpressionAction(scope, st.subTokens, tR);
                    actions.add(expA);
                    ELType t = expA.outType;
                    while (resolvePointer > 0) {
                        if (!t.isResolvable())
                            throw ELAnalysisError.error(
                                    "Unable to resolve non-pointer, address, or array (was " + t.typeString() + ")",
                                    st);
                        actions.add(new DirectAction("LOAD MEM %s %s", str, str));
                        resolvePointer--;
                        t = t.resolve();
                    }
                    if (lastType != null) {
                        if (!t.canCastTo(lastType))
                            throw ELAnalysisError.error(
                                    "Invalid type-cast (" + t.typeString() + " -> " + lastType.typeString() + ")", tkn);
                    } else {
                        lastType = t;
                    }
                    if (lastOp != null) {
                        switch (lastOp) {
                            case ADD -> {
                                actions.add(new DirectAction("ADD %s %s %s", tRegStr, tRegStr, str));
                            }
                            case SUB -> {
                                actions.add(new DirectAction("SUB %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_AND -> {
                                actions.add(new DirectAction("AND %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_OR -> {
                                actions.add(new DirectAction("OR %s %s %s", tRegStr, tRegStr, str));
                            }
                            case BITWISE_NOR -> {
                                actions.add(new DirectAction("NOR %s %s %s", tRegStr, tRegStr, str));
                            }

                            case LEFT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }
                            case RIGHT_SHIFT -> {
                                throw ELAnalysisError.error("Non-constant values not allowed as amount for shift", tkn);
                            }

                            default -> {
                                throw ELAnalysisError.error("Unknown or unsupported operation: " + lastOp + " (exp)",
                                        tkn);
                            }
                        }
                        scope.release(tR);
                    } else {
                        scope.reserve(tR);
                    }
                    not = false;
                    lastOp = null;
                }
                default -> {
                    throw ELAnalysisError.error("Unexpected token found in expression: " + tkn, tkn);
                }
            }
            wI++;
        }
        if (lastType == null)
            throw ELAnalysisError.error("Un-typed expression", tokens.getFirst().startLocation.span(tokens.getLast().endLocation));
        outType = lastType;
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
