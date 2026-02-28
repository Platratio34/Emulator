package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Identifier;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.Token;

public class FunctionAction extends ComplexAction {

    public final int targetReg;
    public ELType retType = null;

    public FunctionAction(ActionScope scope, int targetReg, IdentifierToken it, ErrorSet errors) {
        super(scope);
        this.targetReg = targetReg;
        if(!it.hasParams())
            throw ELAnalysisError.error("Function did not have params", it);

        boolean onStack = true;
        Identifier id = it.asId();
        if (id.starts("SysD")) {
            switch (id.parts[1]) {
                case "memSet" -> {
                    // SysD.memSet(uint32 addr, uint32 value);
                    errors.warning("SysD.memSet is not currently implemented", it);
                    onStack = false;
                }
                case "memCopy" -> {
                    errors.warning("SysD.copy is not currently implemented", it);
                    onStack = false;
                }
                case "interruptReturn" -> {
                    actions.add(new DirectAction("INTERRUPT RET"));
                    scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(), "### `SysD.interruptReturn()`\n\nReturn from an interrupt, resuming execution at the memory address popped to the stack when the interrupt was triggered.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                    return;
                }
                case "halt" -> {
                    actions.add(new DirectAction("HALT"));
                    scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(), "### `SysD.halt()`\n\nHalt the CPU.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                    return;
                }
            }
        }
        // function call; set is parameters
        ArrayList<ELType> types = new ArrayList<>();
        Location endOfParams = null;
        Location startOfParams = it.params.get(0).startLocation;
        Span nameSpan = it.span();
        // boolean vNext = true;
        // boolean addr = false;
        ArrayList<Token> exp = new ArrayList<>();
        int r = onStack ? scope.firstFree() : 1;
        if (!onStack)
            scope.reserve(r);
        boolean[] reserved = new boolean[16];
        for (int i = 0; i < it.params.subTokens.size(); i++) {
            Token t2 = it.params.subTokens.get(i);
            endOfParams = t2.endLocation;
            if (t2 instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                if(exp.isEmpty())
                    throw ELAnalysisError.error("Empty expression", t2);
                if (!onStack && scope.isReserved(r)) {
                    actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                    reserved[r] = true;
                }
                ExpressionAction expA = new ExpressionAction(scope, exp, r);
                actions.add(expA);
                types.add(expA.outType);
                if(onStack)
                    actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                else {
                    // actions.add(new DirectAction("COPY %s %s", MachineCode.translateReg(r),
                    //         MachineCode.translateReg(r++)));
                    scope.reserve(r);
                    r++;
                }
                exp = new ArrayList<>();
            } else {
                exp.add(t2);
            }
        }
        if (!exp.isEmpty()) {
            ExpressionAction expA = new ExpressionAction(scope, exp, r);
            actions.add(expA);
            types.add(expA.outType);
            if (onStack)
                actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
            else
                actions.add(new DirectAction("COPY %s %s", MachineCode.translateReg(r),
                        MachineCode.translateReg(r++)));
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
                case "memSet" -> {
                    // SysD.memSet(uint32 addr, uint32 value);
                    if (types.size() != 2 || !(types.get(0).canCastTo(ELPrimitives.UINT32)
                            && types.get(1).canCastTo(ELPrimitives.UINT32))) {

                        throw ELAnalysisError.error(String.format(
                                "Found no overload of SysD.memSet matching %s; Found SysD.memSet(uint32 addr, uint32 value)",
                                tStr), startOfParams.span(endOfParams));
                    }
                    actions.add(new DirectAction("STORE MEM r1 r2"));
                    for (int i = r-1; i > 0; i--) {
                        if(reserved[r])
                            actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                        else
                            scope.release(i);
                    }
                    return;
                }
                case "memCopy" -> {
                    errors.warning("SysD.copy is not currently implemented", it);

                    /*
                    [r1 = void* src, r2 = uint32 start, r3 = uint32 end, r4 = void* dest, r5 = uint32 destStart]
                    
                    ADD r1 r1 r2 // src += start
                    ADD r4 r4 r5 // dest += destStart
                    SUB r3 r3 r2 // end -= start // end = num elements
                    
                    // r2 = uint32 temp
                    :loopStart
                    LOAD MEM r2 r1 // temp = mem[src]
                    STORE r2 r4 // mem[dest] = temp
                    DEC r3 // end--
                    GOTO GT r3 :loopStart // if(end > 0) goto :loopStart
                    
                    */
                    String loopLabel = String.format("loop_%d", ActionBlock.subIndex++);

                    actions.add(new DirectAction("ADD r1 r1 r2\nADD r4 r4 r5\nSUB r3 r3 r2\n:%s\nLOAD MEM r2 r1\n STORE r2 r4\nINC r3 -1\nGOTO GT r3 :%s", loopLabel, loopLabel));
                    
                    for (int i = r-1; i > 0; i--) {
                        if(reserved[r])
                            actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                        else
                            scope.release(i);
                    }
                    return;
                }
                case "halt" -> {
                    actions.add(new DirectAction("HALT"));
                    return;
                }
                default -> {
                    throw ELAnalysisError.error("Unknown SysD function: `"+id.parts[1]+"`", nameSpan);
                }
            }
        }
        ELFunction f = scope.namespace.findFunction(id, types);
        if (f == null) {

            f = scope.namespace.getFunction(id);
            if (f != null) {
                throw ELAnalysisError.error(String.format(
                        "Found no overload of %s matching %s; Found %s", id.fullName, tStr, f.debugString("")), startOfParams.span(endOfParams));
            } else {
                throw ELAnalysisError.error("Unknown function " + id.fullName + tStr, nameSpan);
            }
        }
        actions.add(new DirectAction("GOTO PUSH :%s", f.getQualifiedName(true)));
        if (f.ret == null) {
            if (onStack)
                actions.add(new DirectAction("STACK DEC %d", types.size()));
        } else if(targetReg >= 0) {
            retType = f.ret;
            if (onStack) {
                actions.add(new DirectAction("STACK DEC %d", types.size()-1));
                actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(targetReg)));
            } else {
                actions.add(new DirectAction("COPY r1 %s", MachineCode.translateReg(targetReg)));
            }
        }
        if (onStack) {
            for (int i = r-1; i > 0; i--) {
                if(reserved[r])
                    actions.add(new DirectAction("STACK POP %s", MachineCode.translateReg(i)));
                else
                    scope.release(i);
            }
        } else {
            scope.release(r);
        }
    }

}
