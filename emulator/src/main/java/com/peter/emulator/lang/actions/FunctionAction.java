package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.Identifier;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ResolveResult;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.SetToken;
import com.peter.emulator.lang.tokens.Token;

public class FunctionAction extends ComplexAction {

    public final Register targetReg;
    public ELType retType = null;

    public FunctionAction(ActionScope scope, Register targetReg, IdentifierToken it) {
        super(scope);
        this.targetReg = targetReg;
        if (!it.hasParamsSub()) {
            throw ELAnalysisError.error("Function did not have params", it);
        }

        boolean onStack = true;
        Identifier id = it.asId();
        if (id.starts("SysD")) {
            switch (id.parts[1]) {
                case "memSet", "memGet", "memCopy" -> {
                    // SysD.memSet(uint32 addr, uint32 value);
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
        Register r = onStack ? scope.firstFree() : scope.makeHandle(1);
        if (!onStack)
            r.reserve();
        boolean[] pushed = new boolean[16];
        boolean[] reserved = new boolean[16];
        ArrayList<Action> tempActions = new ArrayList<>();
        int stackSize = 0;
        if (params.hasSub()) {
            for (int i = 0; i < params.subTokens.size(); i++) {
                Token t2 = params.subTokens.get(i);
                endOfParams = t2.endLocation;
                if (t2 instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                    if (exp.isEmpty())
                        throw ELAnalysisError.error("Empty expression", t2);
                    if (!onStack && r.isReserved()) {
                        tempActions.add(new DirectAction("STACK PUSH %s", r));
                        pushed[r.reg] = true;
                    }
                    ExpressionAction expA = new ExpressionAction(scope, exp, r);
                    tempActions.add(expA);
                    types.add(expA.outType == null ? ELPrimitives.OBJECT : expA.outType);
                    if (onStack) {
                        tempActions.add(new DirectAction("STACK PUSH %s", r));
                        r.release();
                        stackSize += 4;
                    } else {
                        // actions.add(new DirectAction("COPY %s %s", MachineCode.translateReg(r),
                        //         MachineCode.translateReg(r++)));
                        r.reserve();
                        // tempActions.add(new DirectAction("// reserving %s", r));
                        reserved[r.reg] = true;
                        r = r.next();
                    }
                    exp = new ArrayList<>();
                } else {
                    exp.add(t2);
                }
            }
            if (!exp.isEmpty()) {
                if (!onStack && r.isReserved()) {
                    tempActions.add(new DirectAction("STACK PUSH %s", r));
                    pushed[r.reg] = true;
                }
                ExpressionAction expA = new ExpressionAction(scope, exp, r);
                tempActions.add(expA);
                types.add(expA.outType == null ? ELPrimitives.OBJECT : expA.outType);
                if (onStack) {
                    tempActions.add(new DirectAction("STACK PUSH %s", r));
                    r.release();
                    stackSize += 4;
                } else {
                    // tempActions.add(new DirectAction("COPY %s %s", r, r));
                    r.reserve();
                    // tempActions.add(new DirectAction("// reserving %s", r));
                    reserved[r.reg] = true;
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
            actions.addAll(tempActions);
            scope.addSymbol(new ELSymbol.ELNamespaceSymbol("SysD", it.spanFirst()));
            switch (id.parts[1]) {
                case "memSet" -> {
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.FUNCTION_NAME, it.next().spanFirst(),
                            "`constexp void SysD.memSet(void* addr, uint32 value)`\n\nSets the memory at `addr` to `value`"));
                    // void SysD.memSet(uint32 addr, uint32 value);
                    if (types.size() != 2 || !((types.get(0).canCastTo(ELPrimitives.UINT32)
                            || types.get(0).canCastTo(ELPrimitives.VOID_PTR))
                            && types.get(1).canCastTo(ELPrimitives.UINT32))) {

                        throw ELAnalysisError.error(String.format(
                                "Found no overload of SysD.memSet matching %s; Found SysD.memSet(uint32 addr, uint32 value)",
                                tStr), startOfParams.span(endOfParams));
                    }
                    actions.add(new DirectAction("STORE r1 r2"));

                    for (int i = r.reg; i > 0; i--) {
                        if (pushed[i])
                            actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                        else if (reserved[i])
                            scope.release(i);
                    }
                    return;
                }
                case "memGet" -> {
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.FUNCTION_NAME, it.next().spanFirst(),
                            "`constexp uint32 SysD.memGet(void* addr)`\n\nGets the memory at `addr`"));
                    // uint32 SysD.memGet(uint32 addr);
                    if (types.size() != 1 || !(types.get(0).canCastTo(ELPrimitives.UINT32))) {

                        throw ELAnalysisError.error(String.format(
                                "Found no overload of SysD.memSet matching %s; Found SysD.memSet(uint32 addr, uint32 value)",
                                tStr), startOfParams.span(endOfParams));
                    }
                    actions.add(new DirectAction("LOAD r1 %s", targetReg));

                    for (int i = r.reg; i > 0; i--) {
                        if (pushed[i])
                            actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                        else if (reserved[i])
                            scope.release(i);
                    }
                    return;
                }
                case "memCopy" -> {
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.FUNCTION_NAME, it.next().spanFirst(),
                            "`constexp void SysD.memCopy(void* src, uint32 start, uint32 end, void* dest, uint32 destStart)`\n\nCopies the memory from `src + start` through `src + end` to memory starting at `dest + destStart`"));
                    // errors.warning("SysD.copy is not currently implemented", it);

                    /*
                    [r1 = void* src, r2 = uint32 start, r3 = uint32 end, r4 = void* dest, r5 = uint32 destStart]
                    
                    ADD r1 r1 r2 // src += start
                    ADD r4 r4 r5 // dest += destStart
                    SUB r3 r3 r2 // end -= start // end = num elements
                    
                    // r2 = uint32 temp
                    :loopStart
                    COPY MEM r1 r4 // mem[dest] = msm[src]
                    DEC r3 // end--
                    GOTO GT r3 :loopStart // if(end > 0) goto :loopStart
                    
                    */
                    String loopLabel = String.format("loop_%d", ActionBlock.subIndex++);

                    actions.add(new DirectAction(
                            "ADD r1 r1 r2\nADD r4 r4 r5\nSUB r3 r3 r2\n:%s\nCOPY MEM r1 r4 INC_RS INC_RD\nINC r3 -1\nGOTO GT r3 :%s",
                            loopLabel, loopLabel));

                    for (int i = r.reg; i > 0; i--) {
                        if (pushed[i])
                            actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                        else if (reserved[i])
                            scope.release(i);
                    }
                    return;
                }
                case "halt" -> {
                    scope.addSymbol(new ELSymbol(ELSymbol.Type.FUNCTION_NAME, it.next().spanFirst(),
                            "`constexp void SysD.halt()`\n\nHalts execution of the CPU. **MUST BE IN PRIVILEGED MODE TO WORK**"));
                    actions.add(new DirectAction("HALT"));
                    return;
                }
                default -> {
                    throw ELAnalysisError.error("Unknown SysD function: `" + id.parts[1] + "`", nameSpan);
                }
            }
        }

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
        if (rr.function == null) {
            throw ELAnalysisError.errorF(it2.spanFirst(), "`%s` is not a function", it2.value);
        }
        ELFunction f = rr.function.getFunction(types);

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
        
        scope.unit.symbols.add(new ELSymbol.ELFuncCallSymbol(f, it2.spanFirst()));
        if (f.type == FunctionType.INSTANCE)
            addDirect("STACK PUSH r0");
        if (f.ret != null)
            addDirect("STACK INC %d", Math.ceilDiv(f.ret.sizeof(), 4) * 4);
        actions.addAll(tempActions);
        if (f.type == FunctionType.INSTANCE) {
            Register r0T = scope.firstFree();
            ResolveAction rA = scope.loadVarF(it, r0T, false);
            actions.add(rA);
            addDirect("COPY %s r0", r0T);
        }
        actions.add(new DirectAction("GOTO PUSH :%s", f.getQualifiedName(true)));
        if (f.ret == null) {
            if (onStack && stackSize > 0)
                actions.add(new DirectAction("STACK DEC %d", stackSize));
        } else if (targetReg != null) {
            retType = f.ret;
            if (onStack) {
                if (stackSize - 4 > 0)
                    actions.add(new DirectAction("STACK DEC %d", stackSize - 4));
                actions.add(new DirectAction("STACK POP %s", targetReg));
            } else {
                actions.add(new DirectAction("COPY r1 %s", targetReg));
            }
        } else if (onStack && stackSize > 0) {
            actions.add(new DirectAction("STACK DEC %d", stackSize));
        }
        if (f.type == FunctionType.INSTANCE)
            actions.add(new DirectAction("STACK POP r0"));
        if (!onStack) {
            for (int i = r.reg; i > 0; i--) {
                if (pushed[i])
                    actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                else if (reserved[i])
                    scope.release(i);
            }
        } else {
            r.release();
        }
    }

}
