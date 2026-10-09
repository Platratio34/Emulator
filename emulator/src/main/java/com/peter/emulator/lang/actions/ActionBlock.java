package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.assembly.ASMParser;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.lang.ELValue.ELStringValue;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.annotations.ELBreakpointAnnotation;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.symbols.ELAnnotationSymbol;
import com.peter.emulator.lang.symbols.ELStringSymbol;
import com.peter.emulator.lang.symbols.ELVarSymbol;
import com.peter.emulator.lang.symbols.SymbolType;
import com.peter.emulator.lang.tokens.*;

public class ActionBlock extends ComplexAction {

    protected static int subIndex = 0;

    public ActionBlock(ActionScope scope) {
        super(scope);
    }

    public void parse(ArrayList<Token> tokens, ErrorSet errors, boolean withDebug) {
        int wI = 0;
        int l = 0;
        if (scope.function != null) {
            if (scope.function.inline != InlineType.INLINE_RAW) {
                addDirect("STACK PUSH r15");
                addDirect("COPY rStack r15");
            }
            if (scope.function.inline == InlineType.OUTLINE) {
                for (ELVariable var : scope.stackVars.values()) {
                    addDirect("#stackVar %s %s %d", var.type.typeString(), var.name, var.offset);
                }
            }
        }
        int last = -1;
        while (wI < tokens.size()) {
            try {
                Token tkn = tokens.get(wI);

                while ((wI + 1) < tokens.size()
                        && (tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                    wI++;
                    tkn = tokens.get(wI);
                }
                
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
                if (withDebug) {
                    addDirect("#line %s %d:%d", tkn.startLocation.file(), tkn.startLocation.line(), tkn.startLocation.col());
                } else {
                    addDirect("// " + (l++) + " " + tkn.startLocation.line() + ":" + tkn.startLocation.col());
                }
                if (tkn instanceof DocCommentToken tcT) {
                    wI++;
                    continue;
                }
                add(s -> {
                    String str = "";
                    for(int i = 0; i < 16; i++) {
                        if(s.reservedRegisters[i]) {
                            if(str.length() > 0)
                                str += ", ";
                            str += "r" + i; 
                        }
                    }
                    if(str.length() == 0)
                        return null;
                    return "// Still reserved: " + str;
                });
                if (tkn instanceof ASMToken asmT) {
                    wI++;
                    ASMParser asmParser = new ASMParser(scope.unit.module.languageServer.fileProvider, asmT.raw, asmT.startLocation.add(4), true);
                    ELFunction func = scope.getFunction();
                    if(func != null && func.inline != InlineType.OUTLINE) {
                        for(String pName : func.paramOrder) {
                            asmParser.addLintAlias(pName);
                        }
                        if(func.type != ELFunction.FunctionType.STATIC) {
                            asmParser.addLintAlias("this");
                        }
                        if(func.ret != null) {
                            asmParser.addLintAlias("ret");
                        }
                    }
                    boolean asmError = asmParser.parse();
                    scope.addSymbol(SymbolType.KEYWORD, asmT.startLocation.span(2));
                    scope.unit.symbols.addAll(asmParser.getSymbols());
                    // System.out.println("Adding "+asmParser.getSymbols().size()+" symbols from ASM");
                    for (AsmError error : asmParser.errors) {
                        errors.add(new ELAnalysisError(error.severity, error.message, error.span));
                    }
                    if (!asmError) {
                        continue;
                    }
                    String file = asmT.startLocation.file();
                    int lineN = asmT.startLocation.line();
                    int col = asmT.startLocation.col();
                    boolean first = true;
                    for (String line : asmT.raw.stripTrailing().split("\n")) {
                        line = line.replaceAll("\r", "");
                        if (line.length() > 0) {
                            char c = line.charAt(0);
                            while (c == '\t' || c == ' ') {
                                col++;
                                if (line.length() == 1) {
                                    line = "";
                                    break;
                                }
                                line = line.substring(1);
                                c = line.charAt(0);
                            }
                        }
                        if (line.length() > 0) {
                            if(withDebug && !line.startsWith("#"))
                                addDirect("#line %s %d:%d", file, lineN, col);
                            addDirect(line);
                        } else if (!first) {
                            add(Action.blank());
                        }
                        first = false;
                        lineN++;
                        col = 2;
                    }
                    // asmT.addSymbols(scope.unit);
                    continue;
                }
                if (tkn instanceof IdentifierToken it) {
                    Identifier id = it.asId();
                    if (it.hasParamsSub()) {
                        switch (it.value) {
                            case "if" -> {
                                scope.unit.addSymbol(SymbolType.KEYWORD, it.spanFirst());
                                wI += 1;
                                // set is the condition
                                // also block
                                BlockToken iBT;
                                if (tokens.get(wI) instanceof BlockToken bT) {
                                    iBT = bT;
                                } else {
                                    throw ELAnalysisError.error("Expected block after if", tokens.get(wI).span());
                                }
                                wI++;
                                int index = subIndex++;
                                boolean elsePresent = wI < tokens.size()
                                        && tokens.get(wI) instanceof IdentifierToken it3
                                        && it3.value.equals("else");
                                // actions.add(new ConditionalAction(scope, ":if_true_" + index, elsePresent ? (":if_false_" + index) : (":if_end_" + index),
                                //         it.params.subTokens));
                                if (elsePresent) {
                                    scope.addSymbol(SymbolType.KEYWORD, tokens.get(wI).span());
                                }

                                Register r = newRegister();
                                addReserve(r);
                                Expression exp = new Expression(scope, it.params.subTokens, r);
                                exp.setFalseTarget(String.format((elsePresent ? ":if_else_%d" : ":if_end_%d"), index));
                                actions.add(exp);
                                if (!exp.hadGoto()) {
                                    if (elsePresent)
                                        actions.add(new DirectAction("GOTO EQ %s :if_else_%d", r, index));
                                    else
                                        actions.add(new DirectAction("GOTO EQ %s :if_end_%d", r, index));
                                }
                                addRelease(r);
                                // actions.add(new DirectAction(":if_true_%d", index));
                                ActionBlock innerBlock = new ActionBlock(scope.createChild());
                                innerBlock.parse(iBT.subTokens, errors, withDebug);
                                actions.add(innerBlock);
                                if (elsePresent) {
                                    actions.add(new DirectAction("GOTO :if_end_%d", index));
                                    wI++;
                                    if (!(tokens.get(wI) instanceof BlockToken))
                                        throw ELAnalysisError.error("Expected block after else", tokens.get(wI).span());
                                    actions.add(new DirectAction(":if_else_%d", index));
                                    ActionBlock elseBlock = new ActionBlock(scope.createChild());
                                    elseBlock.parse(tokens.get(wI).subTokens, errors, withDebug);
                                    actions.add(elseBlock);
                                    wI++;
                                }

                                actions.add(new DirectAction(":if_end_%d", index));
                                continue;
                            }
                            case "for" -> {
                                scope.unit.addSymbol(SymbolType.KEYWORD, it.spanFirst());
                                wI += 1;
                                // set is (initializer; condition; incrementor)

                                //  iteratorVar = null;
                                int sI = 0;
                                ArrayList<Token> expArr = new ArrayList<>();
                                ActionScope loopScope = scope.createChild();
                                LineAction initializer = new LineAction(loopScope);
                                initializer.usePseudo = true;
                                ComplexAction condition = null;
                                LineAction iterator = null;

                                int index = subIndex++;

                                loopScope.loopExit = String.format(":for_end_%d", index);
                                loopScope.loopContinue = String.format(":for_condition_%d", index);

                                while (sI < it.params.subSize()) {
                                    Token token = it.params.sub(sI++);
                                    if (iterator != null) {
                                        expArr.add(token);
                                    } else if (condition != null) {
                                        if (token instanceof OperatorToken ot) {
                                            if (ot.type == OperatorToken.Type.SEMICOLON) {
                                                iterator = new LineAction(loopScope);

                                                Register r = newRegister();
                                                condition.add(r.reserveAction());
                                                Expression exp = new Expression(loopScope, expArr);
                                                exp.setFalseTarget(loopScope.loopExit);
                                                exp.validate(errors);
                                                condition.add(exp);
                                                if (!exp.hadGoto()) {
                                                    condition.addDirect("GOTO EQ %s %s", r, loopScope.loopExit);
                                                }
                                                condition.add(r.releaseAction());

                                                expArr.clear();
                                                continue;
                                            }
                                        }
                                        expArr.add(token);
                                    } else { // in initializer
                                        if (token instanceof OperatorToken ot) {
                                            if (ot.type == OperatorToken.Type.SEMICOLON) {
                                                condition = new ComplexAction(loopScope);
                                                if (!expArr.isEmpty())
                                                    initializer.parse(expArr);
                                                expArr.clear();
                                                continue;
                                            }
                                        }
                                        expArr.add(token);
                                    }
                                }
                                if (condition == null) {
                                    scope.unit.errors.error("Missing condition and iterator", it.endLocation.span());
                                    wI++;
                                    continue;
                                } else if (iterator == null) {
                                    scope.unit.errors.error("Missing iterator", it.endLocation.span());
                                    wI++;
                                    continue;
                                }
                                if (!expArr.isEmpty()) {
                                    iterator.parse(expArr);
                                }

                                // also block
                                ActionBlock innerBlock = new ActionBlock(loopScope);
                                innerBlock.parse(tokens.get(wI).subTokens, errors, withDebug);
                                // scope.unit.errors.warning("For not currently supported", it);

                                addDirect("// For Loop:\n// Initializer");
                                add(initializer);
                                addDirect(loopScope.loopContinue);
                                add(condition);
                                add(innerBlock);
                                addDirect("// Iterator");
                                add(iterator);
                                addDirect("GOTO %s", loopScope.loopContinue);
                                addDirect(loopScope.loopExit);
                                add(loopScope.getStackResetAction());

                                wI++;
                                continue;
                            }
                            case "while" -> {
                                scope.unit.addSymbol(SymbolType.KEYWORD, it.spanFirst());
                                wI += 1;
                                //set is condition
                                // also block
                                int index = subIndex++;

                                // :while_condition_%d
                                // r[x] = [expression]
                                // GOTO EQ r[x] :while_end_%d
                                // ...body...
                                // :while_end_%d

                                ActionScope loopScope = scope.createChild();
                                loopScope.loopExit = String.format(":while_end_%d", index);
                                loopScope.loopContinue = String.format(":while_condition_%d", index);

                                actions.add(new DirectAction(loopScope.loopContinue));
                                Register r = newRegister();
                                addReserve(r);
                                Expression exp = new Expression(scope, it.params.subTokens, r);
                                exp.setFalseTarget(loopScope.loopExit);
                                actions.add(exp);
                                if (!exp.hadGoto())
                                    actions.add(new DirectAction("GOTO EQ %s %s", r, loopScope.loopExit));
                                addRelease(r);

                                ActionBlock innerBlock = new ActionBlock(loopScope);
                                innerBlock.parse(tokens.get(wI).subTokens, errors, withDebug);
                                actions.add(innerBlock);
                                actions.add(new DirectAction("GOTO %s", loopScope.loopContinue));
                                actions.add(new DirectAction(loopScope.loopExit));
                                wI++;
                                continue;
                            }
                            case "asm" -> {
                                scope.unit.addSymbol(SymbolType.KEYWORD, it.spanFirst());
                                wI += 1;
                                Token t = it.params.get(0);
                                switch (t) {
                                    case null -> throw ELAnalysisError
                                            .error("asm function must have a string literal or const parameter", it);

                                    case StringToken strT -> {
                                        actions.add(new DirectAction(strT.value));
                                        scope.unit.errors
                                                .info("It is recommended to use `asm{...}` for inline assembly.");
                                        scope.addSymbol(new ELStringSymbol(strT));
                                    }

                                    case IdentifierToken it2 -> {
                                        Identifier id2 = it2.asId();
                                        ELVariable var = scope.getVarStack(id2).getLast();
                                        scope.addSymbol(new ELVarSymbol(var, it2.span()));
                                        if (var == null)
                                            throw ELAnalysisError.error("Could not resolve variable " + id2.fullName,
                                                    t.span());
                                        if (var.varType != ELVariable.Type.CONST
                                                || !var.type.equals(ELPrimitives.CHAR.pointerTo())) {
                                            throw ELAnalysisError
                                                    .error("asm function may only take string literal or const", it2);
                                        }
                                        actions.add(new DirectAction(((ELStringValue) var.startingValue).value));
                                    }
                                    default -> throw ELAnalysisError
                                            .error("asm function may only take string literal or const", t);
                                }
                                // if (wI >= tokens.size() || !(tokens.get(wI) instanceof OperatorToken ot
                                //         && ot.type == OperatorToken.Type.SEMICOLON))
                                //     throw ELAnalysisError.error("Missing semicolon",
                                //             tokens.get(wI - 1).endLocation.span());

                                if (wI < tokens.size()) {
                                    if (!(tokens.get(wI) instanceof OperatorToken ot
                                            && ot.type == OperatorToken.Type.SEMICOLON)) {
                                        throw ELAnalysisError
                                                .error("Unexpected token after asm macro, expected ';'",
                                                        tkn.endLocation.span());
                                    }
                                    scope.addSymbol(SymbolType.SEMICOLON, tokens.get(wI).span());
                                    wI++;
                                } else {
                                    throw ELAnalysisError.error("Unexpected end of block after asm macro",
                                            tkn.endLocation.span());
                                }
                                continue;
                            }
                        }
                    }
                } else if (tkn instanceof AnnotationToken at) {
                    if (at.name.equals("Breakpoint")) {
                        ELBreakpointAnnotation bpa = new ELBreakpointAnnotation(at);
                        if (bpa.noOp) {
                            addDirect("NO_OP");
                        }
                        scope.addSymbol(new ELAnnotationSymbol(bpa));
                        addDirect("#breakpoint");
                        wI++;
                        continue;
                    } else {
                        throw ELAnalysisError.error("Unexpected token found at start of expression ("+tkn.debugString()+")", tkn);
                    }
                }
                ArrayList<Token> line = new ArrayList<>();
                while(!(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON) && wI < tokens.size()) {
                    line.add(tkn);
                    wI++;
                    tkn = tokens.get(wI);
                }
                if(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON) {
                    scope.addSymbol(SymbolType.SEMICOLON, tkn.span());
                    wI++;
                    if (!line.isEmpty()) {
                        LineAction lineAction = new LineAction(scope);
                        lineAction.parse(line);
                        add(lineAction);
                    }
                    continue;
                }
                throw ELAnalysisError.error("Unexpected token found at start of expression ("+tkn.debugString()+")", tkn);
            } catch (ELAnalysisError e) {
                Token tkn = (wI >= tokens.size()) ? tokens.getLast() : tokens.get(wI);
                if (e.span == null)
                    e = new ELAnalysisError(e.severity, e.reason, tkn.span());
                errors.add(e);
                while ((wI+1) < tokens.size() && !(tkn instanceof OperatorToken ot && ot.type == OperatorToken.Type.SEMICOLON)) {
                    wI++;
                    tkn = tokens.get(wI);
                }
                scope.freeScopeHandles(errors, tkn.endLocation.span());
                scope.addSymbol(SymbolType.SEMICOLON, tkn.span());
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
        addDirect("// %s\n", line);
        if (withDebug)
            addDirect("#lineend");
        add(s -> {
            String str = "";
            for(int i = 0; i < 16; i++) {
                if(s.reservedRegisters[i]) {
                    if(str.length() > 0)
                        str += ", ";
                    str += "r" + i; 
                }
            }
            if(str.length() == 0)
                return null;
            return "// Reserved: " + str;
        });
        if(scope.function != null) 
            addDirect(":func_exit_" + scope.function.getQualifiedName(true));
        
        // TODO desconstructors here

        if (scope.function != null) {
            if (scope.function.inline != InlineType.INLINE_RAW) {
                actions.add(new DirectAction("COPY r15 rStack"));
                actions.add(new DirectAction("STACK POP r15"));
            }
        } else if (scope.getStackOffDif() > 0) {
            actions.add(scope.getStackResetAction());
        }
        
        if(!tokens.isEmpty())
            scope.freeScopeHandles(errors, tokens.getLast().endLocation.span());
    }

}
