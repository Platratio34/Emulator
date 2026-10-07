package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.PseudoVariable;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.tokens.BlockToken;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.Token;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.MathInstruction;

public class LineAction extends ComplexAction {

    public boolean usePseudo = false;

    public LineAction(ActionScope scope) {
        super(scope);
    }

    public void parse(ArrayList<Token> tokens) {
        if (tokens.isEmpty())
            return;
        if (tokens.getFirst() instanceof IdentifierToken it) {
            switch (it.value) {
                case "new" -> {
                    scope.unit.addSymbol(ELSymbol.Type.KEYWORD, it.spanFirst());
                    scope.unit.errors.error("New not allowed outside of expression", it);
                    return;
                }
                case "delete" -> {
                    scope.unit.addSymbol(ELSymbol.Type.KEYWORD, it.spanFirst());
                    if (tokens.size() == 1) {
                        scope.unit.errors.errorF(it, "Delete requires target");
                        return;
                    }
                    Register reg = newRegister();
                    addReserve(reg);
                    Expression exp = new Expression(scope, new ArrayList<>(tokens.subList(1, tokens.size())), reg);
                    if (!exp.validate(scope.unit.errors)) {
                        return;
                    }
                    if (!exp.getType().isPointer()) {
                        scope.unit.errors.errorF(it,
                                "Invalid type for deleted; Expected pointer but got %s", exp.getType().typeString());
                        return;
                    }
                    add(exp);
                    ELType type = exp.getType().resolve(it.span());
                    ELClass clazz = type.getELClass();
                    if (clazz != null && clazz.destructor != null) {
                        addDirect("STACK PUSH r0\nCOPY %s r0", reg);
                        addDirect("GOTO :%s", clazz.destructor.getQualifiedName(true));
                        addDirect("STACK POP r0");
                    }
                    addDirect("STACK PUSH %s", reg);
                    addDirect("GOTO :Memory.free_void*");
                    addDirect("STACK INC -4");

                    addRelease(reg);
                    return;
                }
                case "return" -> {
                    scope.unit.addSymbol(ELSymbol.Type.KEYWORD, it.spanFirst());
                    ELType ret = scope.getRetType();
                    if (tokens.size() == 1) {
                        if (ret != null) {
                            scope.unit.errors.errorF(it, "Invalid return type; Expected %s", ret.typeString());
                            return;
                        }
                    } else {
                        Register reg = newRegister();
                        addReserve(reg);
                        Expression exp = new Expression(scope, new ArrayList<>(tokens.subList(1, tokens.size())), reg);
                        if (!exp.validate(scope.unit.errors)) {
                            return;
                        }
                        if (!exp.getType().canCastTo(ret)) {
                            scope.unit.errors.errorF(it, "Invalid return type; Expected %s, found %s", ret.typeString(),
                                    exp.getType().typeString());
                            return;
                        }
                        add(exp);
                        int retOffset = scope.getReturnOffset();
                        Register ra = newRegister();
                        addReserve(ra);
                        if (retOffset >= -255) {
                            addDirect("SUB %s r15 %d", ra, -retOffset);
                        } else {
                            addDirect("COPY r15 %s\nINC %s %d", ra, ra, retOffset);
                        }
                        String sizeStr = switch (ret.sizeof()) {
                            case 1 -> " BYTE";
                            case 2 -> " SHORT";
                            default -> "";
                        };
                        addDirect("STORE%s %s %s", sizeStr, reg, ra);
                        addRelease(reg);
                        addRelease(ra);
                    }
                    addDirect("GOTO :func_exit_%s", scope.getFunction().getQualifiedName(true));
                    return;
                }
                case "continue" -> {
                    scope.unit.addSymbol(ELSymbol.Type.KEYWORD, it.spanFirst());
                    if (tokens.size() > 1) {
                        scope.unit.errors.errorF(tokens.get(1).startLocation.span(tokens.getLast().endLocation),
                                "Expected end of line after `continue`");
                    }
                    String loopContinue = scope.getLoopContinue();
                    if (loopContinue == null) {
                        scope.unit.errors.errorF(it, "Can not use `continue` outside of loop");
                        return;
                    }
                    // TODO need to somehow deal w/ stack pointer here
                    addDirect("GOTO %s", loopContinue);
                    return;
                }
                case "break" -> {
                    scope.unit.addSymbol(ELSymbol.Type.KEYWORD, it.spanFirst());
                    if (tokens.size() > 1) {
                        scope.unit.errors.errorF(tokens.get(1).startLocation.span(tokens.getLast().endLocation),
                                "Expected end of line after `break`");
                    }
                    String loopExit = scope.getLoopExit();
                    if (loopExit == null) {
                        scope.unit.errors.errorF(it, "Can not use `break` outside of loop");
                        return;
                    }
                    // TODO need to somehow deal w/ stack pointer here
                    addDirect("GOTO %s", loopExit);
                    return;
                }
            }
            Token token = tokens.getFirst();
            if (scope.hasType(it)) { // new variable definition
                ELType.Builder builder = new ELType.Builder();
                int i2 = 0;
                token = tokens.get(i2++);
                while (builder.ingest(token)) {
                    if (i2 >= tokens.size()) {
                        scope.unit.errors.errorF(token, "Expected variable name after type");
                        return;
                    }
                    token = tokens.get(i2++);
                }
                ELType type = builder.build();
                type.analyze(scope.unit.errors, scope.namespace, scope.unit);
                String name;
                if (token instanceof IdentifierToken it2) {
                    name = it2.value;
                } else {
                    scope.unit.errors.errorF(token, "Expected variable name after type");
                    return;
                }
                ELVariable var;
                if (usePseudo && type.sizeof() <= 4 && !type.isArray()) {
                    Register reg = newRegister();
                    addReserve(reg);
                    var = new PseudoVariable(type, name, scope.namespace, scope.unit,
                            token.startLocation, reg);
                    scope.addVariable(var, scope.unit.errors);
                    scope.unit.symbols.add(new ELVarSymbol(var, token.span()));
                } else {
                    var = scope.addStackVar(name, type, token.startLocation, scope.unit.errors);
                }
                var.analyze(scope.unit.errors, scope.namespace);
                var.defSymbol = scope.addSymbol(new ELVarSymbol(var, token.span()));

                if (i2 >= tokens.size()) {
                    addDirect(String.format("#stackVar %s %s", type.typeString(), var.name));
                    if (!(var instanceof PseudoVariable)) {
                        addDirect("STACK INC %d", type.sizeofWA());
                    }
                    return;
                }
                token = tokens.get(i2++);
                if (!(token instanceof OperatorToken ot && ot.type == OperatorToken.Type.ASSIGN)) {
                    scope.unit.errors.errorF(token, "Expected `=` or end of line");
                    return;
                }
                scope.addSymbol(Type.OPERATOR, token.span());
                if (i2 >= tokens.size()) {
                    scope.unit.errors.errorF(token, "Empty assignment expression");
                    return;
                }
                token = tokens.get(i2);
                if (token instanceof BlockToken bt) { // array assign
                    if (!type.isArray()) {
                        scope.unit.errors.errorF(bt, "Can not assign array to non-array type");
                        return;
                    }
                    ELType innerType = type.resolve(bt.startLocation.span());
                    Register rg = newRegister();
                    addReserve(rg);
                    addDirect(String.format("#stackVar %s %s", type.typeString(), var.name));
                    ArrayList<Token> expArr = new ArrayList<>();
                    int c = 0;
                    for (Token tk : bt.subTokens) {
                        if (tk instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                            c++;
                            if (expArr.isEmpty()) {
                                scope.unit.errors.errorF(ot2, "Empty expression");
                                continue;
                            }
                            Expression exp = new Expression(scope, expArr, rg);
                            expArr.clear();
                            if (!exp.validate(scope.unit.errors))
                                continue;
                            if (!exp.getType().canCastTo(innerType)) {
                                scope.unit.errors.errorF(exp.span(), "Can not cast %s to %s",
                                        exp.getType().typeString(), innerType.typeString());
                                continue;
                            }
                            add(exp);
                            switch (innerType.sizeof()) {
                                case 1 -> {
                                    addDirect("STORE BYTE %s rStack", rg);
                                    addDirect("INC rStack 1");
                                }
                                case 2 -> {
                                    addDirect("STORE SHORT %s rStack", rg);
                                    addDirect("INC rStack 2");

                                }
                                case 4 -> addDirect("STACK PUSH %s", rg);
                            }
                        } else {
                            expArr.add(tk);
                        }
                    }
                    c++;
                    if (expArr.isEmpty()) {
                        scope.unit.errors.errorF(bt.endLocation.span(), "Empty expression");
                        return;
                    }
                    Expression exp = new Expression(scope, expArr, rg);
                    expArr.clear();
                    if (!exp.validate(scope.unit.errors))
                        return;
                    if (!exp.getType().canCastTo(innerType)) {
                        scope.unit.errors.errorF(exp.span(), "Can not cast %s to %s",
                                exp.getType().typeString(), innerType.typeString());
                    }
                    if (c != type.arraySize()) {
                        scope.unit.errors.errorF(bt.endLocation.span(),
                                "Invalid number of elements in array; Expected %d, found %d", type.arraySize(), c);
                    }
                    add(exp);
                    switch (innerType.sizeof()) {
                        case 1 -> {
                            addDirect("STORE BYTE %s rStack", rg);
                            if (c % 4 == 0) {
                                addDirect("INC rStack 1");
                            } else {
                                addDirect("INC rStack %d", 5 - (c % 4));
                            }
                        }
                        case 2 -> {
                            addDirect("STORE SHORT %s rStack", rg);
                            addDirect("INC rStack 2");
                            if (c % 4 == 0) {
                                addDirect("INC rStack 2");
                            } else {
                                addDirect("INC rStack 4");
                            }

                        }
                        case 4 -> addDirect("STACK PUSH %s", rg);
                    }

                    addRelease(rg);
                    return;
                } else if (!(type.isAddress() || type.isAddress() || type.isPointer())) {
                    if (token instanceof IdentifierToken it3 && it3.typeString().equals(type.typeString()) && it3.hasParamsSub()) {
                        // constructor
                        Register rg = newRegister();
                        addReserve(rg);
                        addDirect(String.format("#stackVar %s %s", type.typeString(), var.name));
                        addDirect("COPY rStack %s\nSTACK INC %d", rg, type.sizeofWA());
                        FunctionAction fA = new FunctionAction(scope, null, it3, rg);
                        add(fA);

                        return;
                    }
                }


                Register rg = newRegister();
                addReserve(rg);
                if (tokens.size() - i2 <= 0) {
                    scope.unit.errors.errorF(token, "Empty expression");
                    return;
                }
                Expression exp = new Expression(scope, new ArrayList<>(tokens.subList(i2, tokens.size())), rg);
                addRelease(rg);
                if (!exp.validate(scope.unit.errors))
                    return;
                if (!exp.getType().canCastTo(type)) {
                    scope.unit.errors.errorF(token, "Can not cast %s to %s", exp.getType().typeString(),
                            type.typeString());
                    return;
                }
                add(exp);
                addDirect(String.format("#stackVar %s %s", type.typeString(), var.name));
                switch (type.sizeof()) {
                    case 1 -> addDirect("STACK PUSH BYTE %s", rg);
                    case 2 -> addDirect("STACK PUSH SHORT %s", rg);
                    case 4 -> addDirect("STACK PUSH %s", rg);
                    default -> addDirect("STACK PUSH %s\nSTACK INC %d", rg, type.sizeofWA() - 4);
                }
                addRelease(rg);
                return;
            }
        }
        
        int ti = 0;
        while (ti < tokens.size()) {
            Token token = tokens.get(ti++);
            if (token instanceof OperatorToken ot && ot.type.isAssign()) {
                // ... <==|+=|-=> ...
                
                boolean isAddAssign = ot.type == OperatorToken.Type.ADD_ASSIGN;
                boolean isSubAssign = ot.type == OperatorToken.Type.SUB_ASSIGN;
                boolean isOrAssign = ot.type == OperatorToken.Type.BITWISE_OR_ASSIGN;
                boolean isModAssign = isAddAssign || isSubAssign || isOrAssign;

                // resolve lh
                Register ra = newRegister();
                Register rv = (isModAssign) ? newRegister() : null;
                boolean raLit = false;
                ELType lhType = ELPrimitives.VOID_PTR;
                Span dma = null;
                Token t2;
                if(ti == 0) {
                    scope.unit.errors.errorF(token, "Expected target for assign");
                    return;
                }
                if(tokens.getFirst() instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.POINTER) {
                    dma = ot2.span();
                    if(ti <= 1) {
                        scope.unit.errors.errorF(token, "Expected target for assign");
                        return;
                    }
                    t2 = tokens.get(1);
                } else {
                    t2 = tokens.getFirst();
                }
                IdentifierToken it;
                if(t2 instanceof IdentifierToken it2) {
                    it = it2;
                } else {
                    scope.unit.errors.errorF(t2, "Expected target for assign");
                    return;
                }
                ResolveAction lrA = scope.loadVarAssign(it, ra, dma != null);
                if (lrA != null) {
                    lhType = lrA.returnType;
                    if (dma != null && !(lhType.isPointer() || lhType.isAddress())) {
                        scope.unit.errors.errorF(dma, "Can not use Direct Memory Assign on non-pointer or address");
                    }
                    if (lrA.returnVar != null && (dma == null && (lrA.returnVar.finalVal || lhType.isConstant()))) {
                        scope.unit.errors.errorF(it, "Can not assign to %s",
                                lrA.returnVar.finalVal ? "final variable" : "constant");
                    }
                    if (lrA.returnType.isAddress()) {
                        dma = it.span();
                    }
                    if (dma != null) {
                        lhType = lhType.resolve(dma);
                    }
                    if (isModAssign) {
                        addReserve(rv);
                    }
                    if (lrA.sourceReg != null) {
                        raLit = lrA.regIsValue;
                        ra = lrA.sourceReg;
                        if (isModAssign) {
                            if(raLit)
                                addDirect("COPY %s %s", ra, rv);
                            else {
                                switch (lrA.returnType.sizeof()) {
                                    case 1 -> addDirect("LOAD MEM BYTE %s %s", rv, ra);
                                    case 2 -> addDirect("LOAD MEM SHORT %s %s", rv, ra);
                                    default -> addDirect("LOAD MEM BYTE %s %s", rv, ra);
                                }
                            }
                        }
                    } else {
                        addReserve(ra);
                        add(lrA);
                        if (isModAssign) {
                            addReserve(rv);
                            switch (lhType.sizeof()) {
                                case 1 -> addDirect("LOAD MEM BYTE %s %s", rv, ra);
                                case 2 -> addDirect("LOAD MEM SHORT %s %s", rv, ra);
                                default -> addDirect("LOAD MEM %s %s", rv, ra);
                            }
                        }
                    }
                } else {
                    scope.unit.errors.errorF(it.nameSpan(), "Unable to resolve variable");
                }

                // resolve rh
                Register rg = newRegister();
                int rhCV = 0;
                Expression rh = new Expression(scope, new ArrayList<>(tokens.subList(ti, tokens.size())), rg);
                if (!rh.validate(scope.unit.errors))
                    return;
                if (!rh.getType().canCastTo(lhType)) {
                    scope.unit.errors.errorF(token, "Can not cast %s to %s", rh.getType().typeString(), lhType.typeString());
                    return;
                }
                if(!rh.isConstant()) {
                    addReserve(rg);
                    add(rh);
                }

                // assign rh to lh
                if (raLit) {
                    if(rh.isConstant()) {
                        int c = rh.getConstant();
                        if(isAddAssign) {
                            if (0 <= c && c <= 255) {
                                addDirect("ADD %s %s %d", ra, rv, c);
                            } else if (-255 <= c && c < 0) {
                                addDirect("SUB %s %s %d", ra, rv, -c);
                            } else if (MathInstruction.inIncRange(c)) {
                                addDirect("INC %s %d", rv, c);
                                addDirect("COPY %s %s", rv, ra);
                            } else {
                                addReserve(rg);
                                addDirect("LOAD %s %d", rg, c);
                                addDirect("ADD %s %s %s", ra, rv, rg);
                            }
                        } else if(isSubAssign) {
                            if (0 <= c && c <= 255) {
                                addDirect("SUB %s %s %d", ra, rv, c);
                            } else if (-255 <= c && c < 0) {
                                addDirect("ADD %s %s %d", ra, rv, -c);
                            } else if (MathInstruction.inIncRange(-c)) {
                                addDirect("INC %s %d", rv, -c);
                                addDirect("COPY %s %s", rv, ra);
                            } else {
                                addReserve(rg);
                                addDirect("LOAD %s %d", rg, c);
                                addDirect("SUB %s %s %s", ra, rv, rg);
                            }
                        } else if(isOrAssign) {
                            if(c != 0) {
                                addReserve(rg);
                                addDirect("LOAD %s %d", rg, c);
                                addDirect("OR %s %s %s", ra, rv, rg);
                            }
                        } else {
                            addDirect("LOAD %s %d", ra, c);
                        }
                    } else if (isAddAssign) {
                        addDirect("ADD %s %s %s", ra, rv, rg);
                    } else if (isSubAssign) {
                        addDirect("SUB %s %s %s", ra, rv, rg);
                    } else if (isOrAssign) {
                        addDirect("OR %s %s %s", ra, rv, rg);
                    } else {
                        addDirect("COPY %s %s", rg, ra);
                    }
                } else {
                    if(rh.isConstant()) {
                        int c = rh.getConstant();
                        rg = rv;
                        if(isAddAssign) {
                            if (0 <= c && c <= 255) {
                                addDirect("ADD %s %s %d", rg, rv, c);
                            } else if (-255 <= c && c < 0) {
                                addDirect("SUB %s %s %d", rg, rv, -c);
                            } else if (MathInstruction.inIncRange(c)) {
                                addDirect("INC %s %d", rv, c);
                            } else {
                                Register rt = newRegister();
                                addReserve(rt);
                                addDirect("LOAD %s %d", rt, c);
                                addDirect("ADD %s %s %s", rg, rv, rt);
                                addRelease(rt);
                            }
                        } else if(isSubAssign) {
                            if (0 <= c && c <= 255) {
                                addDirect("SUB %s %s %d", rg, rv, c);
                            } else if (-255 <= c && c < 0) {
                                addDirect("ADD %s %s %d", rg, rv, -c);
                            } else if (MathInstruction.inIncRange(-c)) {
                                addDirect("INC %s %d", rv, -c);
                            } else {
                                Register rt = newRegister();
                                addReserve(rt);
                                addDirect("LOAD %s %d", rt, c);
                                addDirect("SUB %s %s %s", rg, rv, rt);
                                addRelease(rt);
                            }
                        } else if(isOrAssign) {
                            if(c != 0) {
                                Register rt = newRegister();
                                addReserve(rt);
                                addDirect("LOAD %s %d", rt, c);
                                addDirect("OR %s %s %s", rg, rv, rt);
                                addRelease(rt);
                            }
                        } else {
                            // rg = newRegister();
                            // addReserve(rg);
                            // addDirect("LOAD %s %d", rg, c);
                            switch (lhType.sizeof()) {
                                case 1 -> addDirect("STORE BYTE %d %s", c, ra);
                                case 2 -> addDirect("STORE SHORT %d %s", c, ra);
                                default -> addDirect("STORE %d %s", c, ra);
                            }
                            if(lrA.sourceReg == null)
                                addRelease(ra);
                            return;
                        }
                    } else if (isAddAssign) {
                        addDirect("ADD %s %s %s", rg, rv, rg);
                    } else if (isSubAssign) {
                        addDirect("SUB %s %s %s", rg, rv, rg);
                    } else if (isOrAssign) {
                        addDirect("OR %s %s %s", rg, rv, rg);
                    }
                    switch (lhType.sizeof()) {
                        case 1 -> addDirect("STORE BYTE %s %s", rg, ra);
                        case 2 -> addDirect("STORE SHORT %s %s", rg, ra);
                        default -> addDirect("STORE %s %s", rg, ra);
                    }
                    if(lrA.sourceReg == null)
                        addRelease(ra);
                }
                if (rv != null) {
                    addRelease(rv);
                }


                addRelease(rg);

                return;
            }
        }

        // this is not an assignment expression
        Expression exp = new Expression(scope, tokens);
        if (!exp.validate(scope.unit.errors))
            return;
        add(exp);
    }

}
