package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.SetToken;
import com.peter.emulator.lang.tokens.Token;
import com.peter.emulator.machinecode.Reg;

public class FunctionAction extends ComplexAction {

    public Register targetReg;
    public ELType retType = null;
    public boolean isConst = false;
    public boolean isStaticCast = false;
    public int constVal = 0;

    private Register r = null;
    private boolean[] pushed = new boolean[16];
    private ArrayList<Register> aliasedRegisters = new ArrayList<>();
    private boolean retReserved = false;

    public FunctionAction(ActionScope scope, Register targetReg, IdentifierToken it) {
        this(scope, targetReg, it, null);
    }

    public FunctionAction(ActionScope scope, Register target, IdentifierToken it, Register conReg) {
        super(scope);
        targetReg = target;
        if (!it.hasParamsSub()) {
            throw ELAnalysisError.error("Function did not have params", it);
        }

        boolean forceCast = it.value.equals("force_cast");
        if (it.value.equals("cast") || forceCast) {
            if (forceCast) {
                scope.addSymbol(ELSymbol.Type.KEYWORD, it.nameSpan(), "`force_cast<type>(value)`\n\nTells the compiler to treat the value to a particular type, ignoring safety. **NO CONVERSION APPLIED**");
            } else {
                scope.addSymbol(ELSymbol.Type.KEYWORD, it.nameSpan(), "`cast<type>(value)`\n\nTells the compiler to treat the value to a particular type. **NO CONVERSION APPLIED**");
            }
            if (it.types == null) {
                throw ELAnalysisError.error("Must specify target type for cast", it);
            }
            ELType.Builder typeBuild = new ELType.Builder();
            for (Token t : it.types.subTokens) {
                typeBuild.ingest(t);
            }
            ELType targetType = typeBuild.build();
            targetType.analyze(scope.unit.errors, scope.namespace, scope.unit);
            Expression exp = new Expression(scope, it.params.subTokens, targetReg);
            exp.validate(scope.unit.errors);
            if (!forceCast && !exp.getType().canCastTo(targetType)) {
                if(!(targetType.equals(ELPrimitives.INT32) && (exp.getType().isPointer() || exp.getType().isAddress())))
                    throw ELAnalysisError.errorF(it, "Can not cast %s to %s", exp.getType().typeString(),
                            targetType.typeString());
            }
            add(exp);
            retType = targetType;
            isStaticCast = true;
            return;
        } else if (it.value.equals("sizeof")) {
            scope.addSymbol(ELSymbol.Type.KEYWORD, it.nameSpan(), "`sizeof<type?>(variable?)`\n\nReturns the size of a given type. Only type or variable may be provided");
            ELType type;
            if (it.types != null) {
                if (!it.params.subTokens.isEmpty()) {
                    scope.unit.errors.warning("Can not provide both type and variable to sizeof, ignoring variable type.", it.params.span());
                }
                ArrayList<ELType> types = it.getTypes();
                if(types.size() < 1) {
                    throw ELAnalysisError.warning("Must provide target type", it.nameSpan());
                } else if(types.size() > 1) {
                    scope.unit.errors.warning("Must only provide 1 type", types.get(1).location.span(types.getLast().endLocation));
                }
                type = types.getFirst();
                type.analyze(scope.unit.errors, scope.namespace, scope.unit);
            } else {
                if (it.params.subTokens.isEmpty()) {
                    throw ELAnalysisError.warning("Must provide target type", it.nameSpan());
                }
                Expression exp = new Expression(scope, it.params.subTokens, targetReg);
                exp.validate(scope.unit.errors);
                type = exp.getType();
            }
            isConst = true;
            constVal = type.sizeof();
            retType = ELPrimitives.INT32;
            addDirect("LOAD %s %d", r, constVal);
            return;
        }

        boolean onStack = true;
        Identifier id = it.asId();
        if (id.starts("SysD")) {
            switch (id.parts[1]) {
                case "memSet", "memGet", "memCopy" -> {
                    // SysD.memSet(int32 addr, int32 value);
                    // errors.warning("SysD.memSet is not currently implemented", it);
                    onStack = false;
                }
                case "interruptReturn" -> {
                    actions.add(new DirectAction("INTERRUPT RET"));
                    scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(),
                            "### `SysD.interruptReturn()`\n\nReturn from an interrupt, resuming execution at the memory address popped to the stack when the interrupt was triggered.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                    return;
                }
                case "halt" -> {
                    actions.add(new DirectAction("HALT"));
                    scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(),
                            "### `SysD.halt()`\n\nHalt the CPU.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                    return;
                }
            }
        }
        
        boolean isMethodType = false;
        ResolveResult rr = scope.resolveIdentifier(it.value);
        if (rr == null) {
            throw ELAnalysisError.errorF(it.spanFirst(), "Unable to resolve identifier `%s`", it.value);
        }
        IdentifierToken it2 = it;
        while (it2.hasSub()) {
            IdentifierToken itt = it2;
            it2 = it2.next();
            if (rr.namespace != null) {
                scope.addSymbol(new ELSymbol.ELNamespaceSymbol(rr.namespace, itt.spanFirst()));
                rr = rr.namespace.resolveIdentifier(it2.value);
            } else if (rr.variable != null) {
                scope.addSymbol(new ELSymbol.ELVarSymbol(rr.variable, itt.spanFirst()));
                ELClass clazz = rr.variable.type.getELClass();
                if (clazz == null) {
                    throw ELAnalysisError.errorF(it2.spanFirst(), "Encountered variable without class (Type was `%s`)",
                            rr.variable.type.typeString());
                }
                rr = clazz.resolveIdentifier(it2.value, false);
            } else if (rr.function != null) {
                throw ELAnalysisError.errorF(it2.spanFirst(), "Unable to resolve identifier `%s` from function",
                        it2.value);
            }
            if (rr == null) {
                throw ELAnalysisError.errorF(it2.spanFirst(), "Unable to resolve identifier `%s`", it2.value);
            }
        }
        if (rr != null && rr.function != null && rr.function.inline != InlineType.OUTLINE) {
            onStack = false;
        }

        // function call; set is parameters
        ArrayList<ELType> types = new ArrayList<>();
        Location endOfParams = null;
        SetToken params = it.getParamsSub();
        Location startOfParams = params.startLocation;
        Span nameSpan = it.spanFirst();
        // IdentifierToken it2 = it;
        // while (it2.hasSub()) {
        //     scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, nameSpan);
        //     it2 = it2.next();
        //     nameSpan = it2.spanFirst();
        // }
        // boolean vNext = true;
        // boolean addr = false;
        ArrayList<Token> exp = new ArrayList<>();
        r = onStack ? newRegister() : null;
        ArrayList<Action> tempActions = new ArrayList<>();
        int stackSize = 0;
        if (onStack) {
            tempActions.add(new CompilerAction(scope, s -> {
                String str = "";
                for (int i = 1; i < 15; i++) {
                    if (targetReg != null && targetReg.reg == i) {
                        continue;
                    }
                    if (scope.isReserved(i)) {
                        if (str.length() > 0)
                            str += "\n";
                        str += "STACK PUSH r" + i;
                        pushed[i] = true;
                    }
                }
                return str;
            }));
            tempActions.add(r.reserveAction());
        }
        if (params.hasSub()) {
            int pI = 0;
            for (int i = 0; i < params.subTokens.size(); i++) {
                Token t2 = params.subTokens.get(i);
                endOfParams = t2.endLocation;
                if (t2 instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                    if (exp.isEmpty())
                        throw ELAnalysisError.error("Empty expression", t2);
                    if (!onStack) {
                        r = newRegister(rr.function.paramOrder.get(pI++));
                        aliasedRegisters.add(r);
                        tempActions.add(r.reserveAction());
                    }
                    Expression expA = new Expression(scope, exp, r);
                    tempActions.add(expA);
                    types.add(expA.getType() == null ? ELPrimitives.OBJECT : expA.getType());
                    if (onStack) {
                        tempActions.add(new DirectAction(switch (expA.getType().sizeof()) {
                            case 1 -> "STACK PUSH BYTE %s";
                            case 2 -> "STACK PUSH SHORT %s";
                            default -> "STACK PUSH %s";
                        }, r));
                        // tempActions.add(r.releaseAction());
                        stackSize += 4;
                    }
                    exp = new ArrayList<>();
                } else {
                    exp.add(t2);
                }
            }
            if (!exp.isEmpty()) {
                if (!onStack) {
                    r = newRegister(rr.function.paramOrder.get(pI++));
                    aliasedRegisters.add(r);
                    tempActions.add(r.reserveAction());
                }
                Expression expA = new Expression(scope, exp, r);
                tempActions.add(expA);
                types.add(expA.getType() == null ? ELPrimitives.OBJECT : expA.getType());
                if (onStack) {
                    tempActions.add(new DirectAction(switch (expA.getType().sizeof()) {
                        case 1 -> "STACK PUSH BYTE %s";
                        case 2 -> "STACK PUSH SHORT %s";
                        default -> "STACK PUSH %s";
                    }, r));
                    tempActions.add(r.releaseAction());
                    stackSize += 4;
                }
            }
        }

        String tStr = "(";
        for (int i = 0; i < types.size(); i++) {
            if (i > 0)
                tStr += ",";
            tStr += types.get(i).typeString();
        }
        tStr += ")";

        if (id.starts("SysD")) {
            switch (id.parts[1]) {
                case "halt" -> {
                    scope.addSymbol(new ELSymbol.ELNamespaceSymbol("SysD", it.spanFirst()));
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.FUNCTION_NAME, it.next().spanFirst(),
                            "`inline void SysD.halt()`\n\nHalts execution of the CPU. **MUST BE IN PRIVILEGED MODE TO WORK**"));
                    actions.add(new DirectAction("HALT"));
                    return;
                }
                // default -> {
                //     throw ELAnalysisError.error("Unknown SysD function: `" + id.parts[1] + "`", nameSpan);
                // }
            }
        }

        ELFunction f = null;
        if (rr.function != null) {
            f = rr.function.getFunction(types);
        } else if (rr.variable != null && rr.variable.type.getBaseId().equals("method")) {
            isMethodType = true;
            f = ELPrimitives.METHOD_CLASS.function(rr.variable.type);
        } else {
            throw ELAnalysisError.errorF(it2.spanFirst(), "`%s` is not a function", it2.value);
        }

        // ELFunction f = scope.namespace.findFunction(id, types);
        // boolean includedFunction = f == null;
        // if (includedFunction)
        //     f = scope.unit.findFunction(id, types);
        if (f == null) {
            f = rr.function;
            throw ELAnalysisError.error(String.format(
                    "Found no overload of %s matching %s; Found %s", id.fullName, tStr, f.debugString("")),
                    startOfParams.span(endOfParams));
        }
        
        if (isMethodType) {
            scope.unit.symbols.add(new ELSymbol.ELVarSymbol(rr.variable, it2.spanFirst()));
        } else {
            scope.unit.symbols.add(new ELSymbol.ELFuncCallSymbol(f, it2.spanFirst()));
        }

        Register rT = null;
        int retSize = 0;
        if (onStack) {
            if (f.type == FunctionType.INSTANCE || f.type == FunctionType.CONSTRUCTOR)
                addDirect("STACK PUSH r0");
            if (f.ret != null) {
                retSize = Math.ceilDiv(f.ret.sizeof(), 4) * 4;
                addDirect("STACK INC %d", retSize);
            }
        } else {
            if (f.type == FunctionType.INSTANCE) {
                rT = newRegister("this");
                addReserve(rT);
                aliasedRegisters.add(rT);
            }
            if (f.ret != null) {
                if (targetReg == null) {
                    targetReg = newRegister("ret");
                    addReserve(targetReg);
                    retReserved = true;
                }
                add(new CompilerAction(scope, (s) -> {
                    if (!targetReg.reserved) {
                        targetReg.reserve();
                        retReserved = true;
                        return String.format("#alias %s ret", targetReg);
                    }
                    return "";
                }));
            }
        }
        actions.addAll(tempActions);
        if (f.type == FunctionType.INSTANCE) {
            Register r0T = (rT != null) ? rT : newRegister();
            ResolveAction rA = scope.loadVarF(it, r0T, false);
            if (rA.constantValue != null) {
                if (rT == null)
                    rT = new Register(scope, 0);
                addDirect("LOAD %s %s", rT, rA.constantValue);
            } else {
                if (rT == null) {
                    addReserve(r0T);
                    actions.add(rA);
                    addDirect("COPY r0 %s", r0T);
                    addRelease(r0T);
                } else {
                    actions.add(rA);
                }
            }
        } else if (f.type == FunctionType.CONSTRUCTOR) {
            if (conReg == null) {
                throw ELAnalysisError.errorF(it, "Unexpected constructor call");
            }
            addDirect("COPY %s r0", conReg);
        }
        if (isMethodType) {
            Register fP = newRegister();
            addReserve(fP);
            ResolveAction rA = scope.loadVar(it, fP, false);
            actions.add(rA);
            addDirect("GOTO PUSH %s", fP);
            addRelease(fP);
        } else if (f.inline != InlineType.OUTLINE) {
            addDirect("// INLINE START %s", f.getQualifiedName());
            final ELFunction func = f;
            add((s) -> {
                func.actions.scope.copyReserve(s);
                return "";
            });
            actions.add(f.actions);
            addDirect("// INLINE END");
        } else {
            actions.add(new DirectAction("GOTO PUSH :%s", f.getQualifiedName(true)));
        }
        if (f.ret == null) {
            if (onStack && stackSize > 0)
                actions.add(new DirectAction("STACK DEC %d", stackSize));
        } else {
            if (targetReg != null) {
                retType = f.ret;
                if (onStack) {
                    if (stackSize > 0)
                        actions.add(new DirectAction("STACK DEC %d", stackSize));
                    actions.add(new DirectAction(switch (retType.sizeof()) {
                        case 1 -> "STACK POP BYTE %s";
                        case 2 -> "STACK POP SHORT %s";
                        default -> "STACK POP %s";
                    }, targetReg));
                } else {
                    add(new CompilerAction(scope, (s) -> {
                        if (retReserved) {
                            targetReg.release();
                        }
                        return "#alias clear ret";
                    }));
                }
            } else if (onStack && (stackSize + retSize) > 0) {
                actions.add(new DirectAction("STACK DEC %d", stackSize + retSize));
            }
        }
        if (f.type == FunctionType.INSTANCE)
            actions.add(new DirectAction("STACK POP r0"));
        if (!onStack) {
            add(constRelease());
        } else {
            add(new CompilerAction(scope, s -> {
                String str = "";
                for (int i = 15; i > 0; i--) {
                    if (targetReg != null && targetReg.reg == i) {
                        continue;
                    }
                    if (pushed[i]) {
                        if(str.length() > 0)
                            str += "\n";
                        str += "STACK POP r" + i;
                    }
                }
                return str;
            }));
            addRelease(r);
        }
    }

    private ComplexAction constRelease() {
        ComplexAction out = new ComplexAction(scope);
        for (Register r : aliasedRegisters) {
            out.add(r.releaseAction());
        }
        return out;
    }
}
