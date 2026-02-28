package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELValue.ELStringValue;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.tokens.BlockToken;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.StringToken;
import com.peter.emulator.lang.tokens.Token;
import com.peter.emulator.lang.base.ELPrimitives;

public class ActionBlock extends ComplexAction {

    public final ELFunction func;

    protected static int subIndex = 0;

    public ActionBlock(ActionScope scope, ELFunction func) {
        super(scope);
        this.func = func;
    }

    public void parse(ArrayList<Token> tokens, ErrorSet errors) {
        int wI = 0;
        int l = 0;
        if(func != null) {
            actions.add(new DirectAction("STACK PUSH r15"));
            actions.add(new DirectAction("COPY rStack r15"));
        }
        int last = -1;
        while (wI < tokens.size()) {
            try {
                Token tkn = tokens.get(wI);
                // System.out.println(tkn);
                if (last > -1) {
                    String line = "";
                    for (int i = last; i < wI; i++) {
                        Token t2 = tokens.get(i);
                        if (t2.wsBefore())
                            line += " ";
                        line += t2.debugString();
                    }
                    actions.add(new DirectAction("// " + line + "\n"));
                }
                last = wI;
                actions.add(
                        new DirectAction(
                                "// " + (l++) + " " + tkn.startLocation.line() + ":" + tkn.startLocation.col()));
                if (tkn instanceof IdentifierToken it) {
                    Identifier id = it.asId();
                    if (it.hasParams()) {
                        switch (it.value) {
                            case "if" -> {
                                wI += 1;
                                // set is the condition
                                // also block
                                ActionBlock innerBlock = new ActionBlock(scope.createChild(), null);
                                if (!(tokens.get(wI) instanceof BlockToken))
                                        throw ELAnalysisError.error("Expected block after if", tokens.get(wI).span());
                                innerBlock.parse(tokens.get(wI).subTokens, errors);
                                wI++;
                                int index = subIndex++;
                                boolean elsePresent = wI < tokens.size() && tokens.get(wI) instanceof IdentifierToken it3
                                        && it3.value.equals("else");
                                // actions.add(new ConditionalAction(scope, ":if_true_" + index, elsePresent ? (":if_false_" + index) : (":if_end_" + index),
                                //         it.params.subTokens));

                                int r = scope.firstFree();
                                scope.reserve(r);
                                actions.add(new ExpressionAction(scope, it.params.subTokens, r));
                                // actions.add(new ConditionalAction(scope, ":while_body_"+index, ":while_end_"+index, it.params.subTokens));
                                // actions.add(new DirectAction(":while_body_%d",index));
                                if(elsePresent)
                                    actions.add(new DirectAction("GOTO EQ %s :if_else_%d", MachineCode.translateReg(r), index));
                                else
                                    actions.add(new DirectAction("GOTO EQ %s :if_end_%d", MachineCode.translateReg(r), index));
                                
                                // actions.add(new DirectAction(":if_true_%d", index));
                                actions.add(innerBlock);
                                if (elsePresent) {
                                    actions.add(new DirectAction("GOTO :if_end_%d", index));
                                    wI++;
                                    if (!(tokens.get(wI) instanceof BlockToken))
                                        throw ELAnalysisError.error("Expected block after else", tokens.get(wI).span());
                                    actions.add(new DirectAction(":if_else_%d", index));
                                    ActionBlock elseBlock = new ActionBlock(scope.createChild(), null);
                                    elseBlock.parse(tokens.get(wI).subTokens, errors);
                                    actions.add(elseBlock);
                                    wI++;
                                }
                                
                                actions.add(new DirectAction(":if_end_%d",index));
                                continue;
                            }
                            case "for" -> {
                                wI += 1;
                                // set is (initializer; condition; incrementor)
                                // also block
                                ActionBlock innerBlock = new ActionBlock(scope.createChild(), null);
                                innerBlock.parse(tokens.get(wI).subTokens, errors);
                                wI++;
                                continue;
                            }
                            case "while" -> {
                                wI += 1;
                                //set is condition
                                // also block
                                ActionBlock innerBlock = new ActionBlock(scope.createChild(), null);
                                innerBlock.parse(tokens.get(wI).subTokens, errors);
                                int index = subIndex++;

                                // :while_condition_%d
                                // r[x] = [expression]
                                // GOTO EQ r[x] :while_end_%d
                                // ...body...
                                // :while_end_%d
                                
                                actions.add(new DirectAction(":while_condition_%d", index));
                                int r = scope.firstFree();
                                scope.reserve(r);
                                actions.add(new ExpressionAction(scope, it.params.subTokens, r));
                                // actions.add(new ConditionalAction(scope, ":while_body_"+index, ":while_end_"+index, it.params.subTokens));
                                // actions.add(new DirectAction(":while_body_%d",index));
                                actions.add(new DirectAction("GOTO EQ %s :while_end_%d", MachineCode.translateReg(r), index));
                                actions.add(innerBlock);
                                actions.add(new DirectAction("GOTO :while_condition_%d",index));
                                actions.add(new DirectAction(":while_end_%d",index));
                                wI++;
                                continue;
                            }
                            case "asm" -> {
                                wI += 1;
                                Token t = it.params.get(0);
                                switch (t) {
                                    case null -> throw ELAnalysisError.error("asm function must have a string literal or const parameter", it);

                                    case StringToken strT -> actions.add(new DirectAction(strT.value));

                                    case IdentifierToken it2 -> {
                                        Identifier id2 = it2.asId();
                                        ELVariable var = scope.getVarStack(id2).getLast();
                                        if(var == null)
                                            throw ELAnalysisError.error("Could not resolve variable "+id2.fullName, t.span());
                                        if(var.varType != ELVariable.Type.CONST || !var.type.equals(ELPrimitives.CHAR.pointerTo())) {
                                            throw ELAnalysisError.error("asm function may only take string literal or const", it2);
                                        }
                                        actions.add(new DirectAction(((ELStringValue)var.startingValue).value));
                                    }
                                    default -> throw ELAnalysisError.error("asm function may only take string literal or const", t);
                                }
                                if (!(tokens.get(wI) instanceof OperatorToken ot
                                        && ot.type == OperatorToken.Type.SEMICOLON))
                                    throw ELAnalysisError.error("Missing semicolon",
                                            tokens.get(wI - 1).endLocation.span());
                                continue;
                            }
                            default -> { // function call
                                actions.add(new FunctionAction(scope, -1, it, errors));
                                // boolean onStack = true;
                                wI += 1;
                                // if (id.starts("SysD")) {
                                //     switch (id.parts[1]) {
                                //         case "memSet" -> {
                                //             // SysD.memSet(uint32 addr, uint32 value);
                                //             errors.warning("SysD.memSet is not currently implemented", it);
                                //             onStack = false;
                                //         }
                                //         case "memCopy" -> {
                                //             errors.warning("SysD.copy is not currently implemented", it);
                                //             continue;
                                //         }
                                //         case "interruptReturn" -> {
                                //             actions.add(new DirectAction("INTERRUPT RET"));
                                //             scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(), "### `SysD.interruptReturn()`\n\nReturn from an interrupt, resuming execution at the memory address popped to the stack when the interrupt was triggered.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                                //             continue;
                                //         }
                                //         case "halt" -> {
                                //             actions.add(new DirectAction("HALT"));
                                //             scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it.span(), "### `SysD.halt()`\n\nHalt the CPU.\n\n**ONLY USE IN LOW-LEVEL PROGRAMMING**. *Privileged Mode only*");
                                //             continue;
                                //         }
                                //     }
                                // }
                                // // function call; set is parameters
                                // ArrayList<ELType> types = new ArrayList<>();
                                // Location endOfParams = null;
                                // Location startOfParams = it.params.get(0).startLocation;
                                // Span nameSpan = it.span();
                                // // boolean vNext = true;
                                // // boolean addr = false;
                                // ArrayList<Token> exp = new ArrayList<>();
                                // int r = 2;
                                // for (int i = 0; i < it.params.subTokens.size(); i++) {
                                //     Token t2 = it.params.subTokens.get(i);
                                //     endOfParams = t2.endLocation;
                                //     if (t2 instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                                //         if(exp.isEmpty())
                                //             throw ELAnalysisError.error("Empty expression", t2);
                                //         ExpressionAction expA = new ExpressionAction(scope, exp, r);
                                //         actions.add(expA);
                                //         types.add(expA.outType);
                                //         if(onStack)
                                //             actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                                //         else
                                //             actions.add(new DirectAction("COPY %s %s", MachineCode.translateReg(r),
                                //                     MachineCode.translateReg(r++)));
                                //         exp = new ArrayList<>();
                                //     } else {
                                //         exp.add(t2);
                                //     }
                                // }
                                // if (!exp.isEmpty()) {
                                //     ExpressionAction expA = new ExpressionAction(scope, exp, r);
                                //     actions.add(expA);
                                //     types.add(expA.outType);
                                //     if (onStack)
                                //         actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                                //     else
                                //         actions.add(new DirectAction("COPY %s %s", MachineCode.translateReg(r),
                                //                 MachineCode.translateReg(r++)));
                                // }

                                // String tStr = "(";
                                // for (int i = 0; i < types.size(); i++) {
                                //     if (i > 0)
                                //         tStr += ",";
                                //     tStr += types.get(i).typeString();
                                // }
                                // tStr += ")";

                                // if (id.starts("SysD")) {
                                //     switch (id.parts[1]) {
                                //         case "memSet" -> {
                                //             // SysD.memSet(uint32 addr, uint32 value);
                                //             if (types.size() != 2 || !(types.get(0).canCastTo(ELPrimitives.UINT32)
                                //                     && types.get(1).canCastTo(ELPrimitives.UINT32))) {

                                //                 throw ELAnalysisError.error(String.format(
                                //                         "Found no overload of SysD.memSet matching %s; Found SysD.memSet(uint32 addr, uint32 value)",
                                //                         tStr), startOfParams.span(endOfParams));
                                //             }
                                //             actions.add(new DirectAction("STORE MEM r1 r2"));
                                //             continue;
                                //         }
                                //         case "memCopy" -> {
                                //             errors.warning("SysD.copy is not currently implemented", it);
                                //             continue;
                                //         }
                                //         case "halt" -> {
                                //             actions.add(new DirectAction("HALT"));
                                //             continue;
                                //         }
                                //         default -> {
                                //             throw ELAnalysisError.error("Unknown SysD function: `"+id.parts[1]+"`", nameSpan);
                                //         }
                                //     }
                                // }
                                // ELFunction f = scope.namespace.findFunction(id, types);
                                // if (f == null) {

                                //     f = scope.namespace.getFunction(id);
                                //     if (f != null) {
                                //         throw ELAnalysisError.error(String.format(
                                //                 "Found no overload of %s matching %s; Found %s", id.fullName, tStr, f.debugString("")), startOfParams.span(endOfParams));
                                //     } else {
                                //         throw ELAnalysisError.error("Unknown function " + id.fullName + tStr, nameSpan);
                                //     }
                                // }
                                // actions.add(new DirectAction("GOTO PUSH :%s", f.getQualifiedName(true)));
                                // actions.add(new DirectAction("STACK DEC %d", types.size()));
                                // STACK PUSH param 0
                                // GOTO PUSH :funcName_paramTypes
                                // STACK DEC 1
                                continue;
                            }
                        }
                    }
                    switch (id.fullName) {
                        case "new" -> {
                            wI++;
                            tkn = tokens.get(wI);
                            while(!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                                tkn = tokens.get(wI++);
                            }
                            continue;
                        }
                        case "delete" -> {
                            wI++;
                            tkn = tokens.get(wI);
                            while(!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                                tkn = tokens.get(wI++);
                            }
                            continue;
                        }
                        case "return" -> {
                            wI++;
                            tkn = tokens.get(wI);
                            while (!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                                if (wI == tokens.size())
                                    throw ELAnalysisError.error("Expected semicolon", tokens.get(wI-1).endLocation.span());
                                tkn = tokens.get(wI++);
                            }
                            continue;
                        }
                        case "continue" -> {
                            wI++;
                            tkn = tokens.get(wI);
                            if(!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON))
                                throw ELAnalysisError.error("Expected semicolon", tkn);
                            continue;
                        }
                        case "break" -> {
                            wI++;
                            tkn = tokens.get(wI);
                            if(!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON))
                                throw ELAnalysisError.error("Expected semicolon", tkn);
                            continue;
                        }
                    }

                    if (!scope.hasVariable(id) && !id.first().equals("SysD")) {
                        ELType.Builder b = new ELType.Builder();
                        tkn = tokens.get(wI++);
                        while (b.ingest(tkn)) {
                            if (tokens.size() == wI) {
                                throw ELAnalysisError.error("Unexpected end of block", tkn);
                            }
                            tkn = tokens.get(wI++);
                        }
                        ELType type = b.build();
                        String name;
                        if (tkn instanceof IdentifierToken it2) {
                            name = it2.value;
                        } else {
                            throw ELAnalysisError
                                    .error("Expected variable name identifier (found " + tkn.debugString() + ")", tkn);
                        }
                        ELVariable var = scope.addStackVar(name, type, tokens.get(wI - 1).endLocation, errors);
                        var.analyze(errors, scope.namespace);
                        scope.unit.symbols.add(new ELVarSymbol(var, tkn.span()));

                        tkn = tokens.get(wI);
                        if (tkn instanceof OperatorToken ot) {
                            switch (ot.type) {
                                case SEMICOLON -> {
                                    wI++;
                                    actions.add(new StackAllocAction(scope, var, -1));
                                }
                                case ASSIGN -> {
                                    wI++;
                                    tkn = tokens.get(wI++);
                                    if (tkn instanceof BlockToken bt) {
                                        if(!type.isArray())
                                            throw ELAnalysisError.error("Block only allowed for array declaration", bt);
                                        if(!(tokens.get(wI) instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.SEMICOLON)) {
                                            throw ELAnalysisError.error("Expected `;` after array declaration", tkn);
                                        }
                                        ArrayList<Token> expTkns = new ArrayList<>();
                                        int n = 0;
                                        for (int i = 0; i < bt.subSize(); i++) {
                                            Token t = bt.subTokens.get(i);
                                            if (t instanceof OperatorToken ot3
                                                    && ot3.type == OperatorToken.Type.COMMA) {
                                                if (expTkns.isEmpty())
                                                    throw ELAnalysisError.error("Empty expression", t);
                                                int r = scope.firstFree();
                                                actions.add(new ExpressionAction(scope, expTkns, r));
                                                actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                                                expTkns = new ArrayList<>();
                                                n++;
                                            } else {
                                                expTkns.add(t);
                                            }
                                        }
                                        if(expTkns.isEmpty())
                                            throw ELAnalysisError.error("Empty expression", bt);
                                        int r = scope.firstFree();
                                        actions.add(new ExpressionAction(scope, expTkns, r));
                                        actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                                        n++;
                                        if (n != type.arraySize()) {
                                            throw ELAnalysisError.error("Array size mismatch; (Declared as "+type.arraySize()+" elements, but "+n+" initialized)", bt);
                                        }
                                    } else {
                                        ArrayList<Token> exp = new ArrayList<>();
                                        while (!(tkn instanceof OperatorToken ot2
                                                && ot2.type == OperatorToken.Type.SEMICOLON)) {
                                            exp.add(tkn);
                                            if (wI + 1 == tokens.size())
                                                throw ELAnalysisError.error("Unexpected end of block. Expected `;`",
                                                        tkn);
                                            tkn = tokens.get(wI++);
                                        }
                                        if (exp.isEmpty())
                                            throw ELAnalysisError.error("Empty expression", tkn);
                                        int r = scope.firstFree();
                                        actions.add(new ExpressionAction(scope, exp, r));
                                        actions.add(new StackAllocAction(scope, var, r));
                                        scope.release(r);
                                    }
                                }
                                default ->
                                    throw ELAnalysisError.error("Expected `;` or `=` (found `" + ot.type + "`)", tkn);
                            }
                        } else {
                            throw ELAnalysisError.error("Expected `;` or `=` (found " + tkn + ")", tkn);
                        }
                        continue;
                    }
                    
                    

                    IdentifierToken targetVal = it;
                    wI++;
                    tkn = tokens.get(wI);

                    if (tkn instanceof OperatorToken ot && (ot.type == OperatorToken.Type.ASSIGN || ot.type == OperatorToken.Type.ADD_ASSIGN || ot.type == OperatorToken.Type.SUB_ASSIGN
                            || ot.type == OperatorToken.Type.INC || ot.type == OperatorToken.Type.DEC)) {
                        Span actionSpan = tkn.span();
                        int rT = scope.firstFree();
                        scope.reserve(rT);
                        String rTStr = MachineCode.translateReg(rT);
                        ELType t;
                        if (targetVal.value.equals("SysD")) {
                            scope.addSymbol(new ELSymbol(ELSymbol.Type.NAMESPACE_NAME, it.spanFirst(), "### `SysD`\nSystem Direct Low-level module"));
                            if(!targetVal.hasSub() || targetVal.subTokens.size() != 1)
                                throw ELAnalysisError.error("Unable to resolve variable `"+it.debugString()+"`", it);
                            String vN = targetVal.sub(0).value;
                            if(vN.startsWith("r")) {
                                switch(vN) {
                                    case "rPM" -> {
                                        actions.add(new DirectAction("COPY rPM %s", rTStr));
                                        t = ELPrimitives.BOOL;
                                    }
                                    case "rStack", "rMemTbl" -> {
                                        actions.add(new DirectAction("COPY %s %s", vN, rTStr));
                                        t = ELPrimitives.VOID_PTR;
                                    }
                                    case "rID" -> {
                                        actions.add(new DirectAction("COPY %s %s", vN, rTStr));
                                        t = ELPrimitives.VOID_PTR;
                                        throw ELAnalysisError.error("`rID` is read-only", targetVal.span());
                                    }
                                    default -> {
                                        actions.add(new DirectAction("COPY %s %s", vN, rTStr));
                                        t = ELPrimitives.UINT32;
                                    }
                                }
                                scope.addSymbol(new ELSymbol(ELSymbol.Type.VARIABLE_NAME, it.sub(0).span(), "### `%s %s`\nCPU register `%s`\n\n"+MachineCode.regDesc(vN), t.typeString(), vN, vN));
                            } else {
                                throw ELAnalysisError.error("Unknown SysD variable `" + vN + "`", it);
                            }
                        } else {
                            ResolveAction rA = scope.loadVar(targetVal, rT, false);
                            if (rA == null) // block stack var
                                throw ELAnalysisError.error("Unable to resolve variable `"+targetVal.debugString()+"`", it.span());
                            t = rA.returnType;
                            if(rA.returnVar.finalVal || t.isConstant())
                                throw ELAnalysisError.error("Cannot assign to "+(rA.returnVar.finalVal ? "final variable" : "constant"), it.startLocation.span(actionSpan.end()));
                            actions.add(rA);
                        }

                        if (ot.type == OperatorToken.Type.INC) {
                            if(!(t.isPointer() || t.equals(ELPrimitives.UINT32)))
                                throw ELAnalysisError.error("Unable to increment type " + t.typeString(), it.span());
                            int r2 = scope.firstFree();
                            String r2Str = MachineCode.translateReg(r2);
                            actions.add(new DirectAction("LOAD MEM %s %s", r2Str, rTStr));
                            actions.add(new DirectAction("INC %s 1", r2Str));
                            actions.add(new DirectAction("STORE %s %s", r2Str, rTStr));
                            wI += 2;
                            continue;
                        } else if (ot.type == OperatorToken.Type.DEC) {
                            if(!(t.isPointer() || t.equals(ELPrimitives.UINT32)))
                                throw ELAnalysisError.error("Unable to decrement type " + t.typeString(), it.span());
                            int r2 = scope.firstFree();
                            String r2Str = MachineCode.translateReg(r2);
                            actions.add(new DirectAction("LOAD MEM %s %s", r2Str, rTStr));
                            actions.add(new DirectAction("INC %s -1", r2Str));
                            actions.add(new DirectAction("STORE %s %s", r2Str, rTStr));
                            wI += 2;
                            continue;
                        }

                        if(t.isAddress()) {
                            actions.add(new DirectAction("LOAD MEM %s %s", rTStr, rTStr)); // resolve the address
                            t = t.resolve();
                        }
                        
                        ArrayList<Token> exp = new ArrayList<>();
                        wI++;
                        tkn = tokens.get(wI++);
                        while (!(tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.SEMICOLON)) {
                            exp.add(tkn);
                            if (wI == tokens.size())
                                throw ELAnalysisError.error("Unexpected end of block. Expected `;` " + tkn, tkn);
                            tkn = tokens.get(wI++);
                        }
                        wI--;
                        if(exp.isEmpty())
                            throw ELAnalysisError.error("Empty expression", tkn);
                        
                        scope.reserve(rT);
                        int r = scope.firstFree();
                        ExpressionAction expA = new ExpressionAction(scope, exp, r);
                        actions.add(expA);

                        if(!expA.outType.canCastTo(t))
                            throw ELAnalysisError.error("Invalid assign, can not cast " + expA.outType.typeString() + " to " + t.typeString(), it.startLocation.span(actionSpan.end()));

                        if (ot.type == OperatorToken.Type.ADD_ASSIGN) {
                            int r2 = scope.firstFree();
                            actions.add(new DirectAction("LOAD MEM %s %s", MachineCode.translateReg(r2), rTStr));
                            actions.add(new DirectAction("ADD %s %s %s", rTStr, MachineCode.translateReg(r2), rTStr));
                        } else if (ot.type == OperatorToken.Type.SUB_ASSIGN) {
                            int r2 = scope.firstFree();
                            actions.add(new DirectAction("LOAD MEM %s %s", MachineCode.translateReg(r2), rTStr));
                            actions.add(new DirectAction("SUB %s %s %s", rTStr, MachineCode.translateReg(r2), rTStr));
                        }
                        actions.add(new DirectAction("STORE %s %s", MachineCode.translateReg(r), rTStr));
                        scope.release(rT);
                        scope.release(r);
                        
                    }
                } else {
                    if(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON) {
                        wI++;
                        continue;
                    }
                    throw ELAnalysisError.error("Unexpected token "+tkn.debugString(), tkn);
                }
            } catch (ELAnalysisError e) {
                Token tkn = tokens.get((wI >= tokens.size()) ? wI-1 : wI);
                if (e.span == null)
                    e = new ELAnalysisError(e.severity, e.reason, tkn.span());
                errors.add(e);
                while ((wI+1) < tokens.size() && !(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                    wI++;
                    tkn = tokens.get(wI);
                }
            }
            wI++;
        }

        String line = "";
        if (last == -1)
            last = 0;
        for (int i = last; i < tokens.size(); i++) {
            Token t2 = tokens.get(i);
            if (t2.wsBefore())
                line += " ";
            line += t2.debugString();
        }
        actions.add(new DirectAction("// " + line + "\n"));
        if(scope.getStackOffDif() > 0)
            actions.add(scope.getStackResetAction());
        if(func != null)
            actions.add(new DirectAction("STACK POP r15"));
    }

    public static int getSysDReg(Identifier id) {
        return switch(id.parts[1]) {
            case "r0" -> 0;
            case "r1" -> 1;
            case "r2" -> 2;
            case "r3" -> 3;
            case "r4" -> 4;
            case "r5" -> 5;
            case "r6" -> 6;
            case "r7" -> 7;
            case "r8" -> 8;
            case "r9" -> 9;
            case "r10" -> 10;
            case "r11" -> 11;
            case "r12" -> 12;
            case "r13" -> 13;
            case "r14" -> 14;
            case "r15" -> 15;
            case "rPgm" -> MachineCode.REG_PGM_PNTR;
            case "rStack" -> MachineCode.REG_STACK_PNTR;
            case "rPid" -> MachineCode.REG_PID;
            case "rMemTbl" -> MachineCode.REG_MEM_TABLE;
            case "rIC" -> MachineCode.REG_INTERRUPT;
            case "rIH" -> MachineCode.REG_INTR_HANDLER;
            case "rPM" -> MachineCode.REG_PRIVILEGED_MODE;
            case "rID" -> MachineCode.REG_CPU_ID;
            default -> -1;
        };
    }

}
