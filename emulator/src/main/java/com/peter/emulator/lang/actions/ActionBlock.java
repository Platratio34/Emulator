package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.NumberToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.SetToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ActionBlock extends Action {

    public final ArrayList<Action> actions = new ArrayList<>();
    public final ActionScope scope;

    public ActionBlock(ActionScope scope) {
        this.scope = scope;
    }

    public void parse(ArrayList<Token> tokens, ErrorSet errors) {
        int wI = 0;
        int l = 0;
        actions.add(new DirectAction("COPY rStack r15"));
        int last = -1;
        while(wI < tokens.size()) {
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
                actions.add(new DirectAction("// " + line+"\n"));
            }
            last = wI;
            actions.add(
                    new DirectAction("// " + (l++) + " " + tkn.startLocation.line() + ":" + tkn.startLocation.col()));
            if(tkn instanceof IdentifierToken it) {
                if(wI+1 < tokens.size() && tokens.get(wI+1) instanceof SetToken st) {
                    switch (it.value) {
                        case "if" ->                             {
                            // set is the condition
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens, errors);
                            continue;
                        }
                        case "for" ->                             {
                            // set is (initilizer; condition; incrementer)
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens, errors);
                            continue;
                        }
                        case "while" ->                             {
                            //set is condition
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens, errors);
                            continue;
                        }
                        default -> {
                            wI += 2;
                            // function call; set is parameters
                            actions.add(new DirectAction("STACK PUSH r15"));
                            ArrayList<ELType> types = new ArrayList<>();
                            for(int i = 0; i < st.subTokens.size(); i++) {
                                Token t2 = st.subTokens.get(i);
                                if (t2 instanceof IdentifierToken it2) {
                                    Identifier id2 = it2.asId();
                                    if(scope.loadVar(id2, 2, actions, true)) {
                                        actions.add(new DirectAction("STACK PUSH r2"));
                                        types.add(scope.getVar(id2).type);
                                    } else if(it2.value.equals("SysD")) {
                                        int r = getSysDReg(id2);
                                        if(r == -1)
                                            throw ELAnalysisError.error("Unable to resolve SysD variable "+id2);
                                        actions.add(new DirectAction("STACK PUSH %s", MachineCode.translateReg(r)));
                                        types.add(ELPrimitives.UINT32);
                                    }
                                }
                            }
                            Identifier id = new Identifier(it);
                            ELFunction f = scope.namespace.findFunction(id, types);
                            if(f == null) {
                                String tStr = "(";
                                for(int i = 0; i < types.size(); i++) {
                                    if(i > 0)
                                        tStr += ",";
                                    tStr += types.get(i).typeString();
                                }
                                tStr += ")";
                                throw ELAnalysisError.error("Unknown function "+id.fullName+tStr, it.span());
                            }
                            actions.add(new DirectAction("GOTO PUSH :%s", f.getQualifiedName(true)));
                            actions.add(new DirectAction("STACK DEC %d", types.size()));
                            actions.add(new DirectAction("STACK POP r15"));
                            // STACK PUSH param 0
                            // GOTO PUSH :funcName_paramTypes
                            // STACK DEC 1
                            continue;
                        }
                    }
                }
                
                if(it.value.equals("uint32")) {
                    ELType.Builder b = new ELType.Builder();
                    tkn = tokens.get(wI++);
                    while(b.ingest(tkn)) {
                        tkn = tokens.get(wI++);
                    }
                    ELType type = b.build();
                    String name;
                    if(tkn instanceof IdentifierToken it2) {
                        name = it2.value;
                    } else {
                        throw ELAnalysisError.error("Expected identifier (found "+tkn+")", tkn.span());
                    }
                    tkn = tokens.get(wI);
                    scope.addStackVar(name, type, errors);
                    if(tkn instanceof OperatorToken ot) {
                        switch (ot.type) {
                            case SEMICOLON -> {
                                wI++;
                                actions.add(new StackAllocAction(-1));
                            }
                            case ASSIGN -> wI++;
                            default -> throw ELAnalysisError.error("Expected `;` or `=` (found `"+ot.type+"`)", tkn.span());
                        }
                    } else {
                        throw ELAnalysisError.error("Expected `;` or `=` (found "+tkn+")", tkn.span());
                    }
                    continue;
                }
                Identifier targetVal = it.asId();
                wI++;
                tkn = tokens.get(wI);
                if(tkn instanceof OperatorToken ot && (ot.type == OperatorToken.Type.ASSIGN || ot.type == OperatorToken.Type.INC || ot.type == OperatorToken.Type.DEC)) {                    
                    if(!scope.loadVar(targetVal, 1, actions, false)) { // block stack var
                        throw ELAnalysisError.error("Unable to resolve variable "+targetVal, it.span());
                    }

                    if(ot.type == OperatorToken.Type.INC) {
                        actions.add(new DirectAction("LOAD MEM r2 r1\nINC r2 1\nSTORE r2 r1"));
                        wI+=2;
                        continue;
                    } else if(ot.type == OperatorToken.Type.DEC) {
                        actions.add(new DirectAction("LOAD MEM r2 r1\nINC r2 -1\nSTORE r2 r1"));
                        wI+=2;
                        continue;
                    }

                    wI++;
                    tkn = tokens.get(wI++);
                    // need an expression here
                    ArrayList<Token> exp = new ArrayList<>();
                    while(!(tkn instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.SEMICOLON)) {
                        // System.out.println("- "+tkn);
                        exp.add(tkn);
                        tkn = tokens.get(wI++);
                    }
                    wI--;
                    int eI = 0;
                    if(exp.get(0) instanceof OperatorToken ot3) {
                        if(ot3.type == OperatorToken.Type.POINTER) {// pointer de-ref
                            eI++;
                        } else if(ot3.type == OperatorToken.Type.BITWISE_AND) {// address-of
                            eI++;
                        }
                    }
                    int srcReg = 0;
                    tkn = exp.get(eI);
                    
                    switch (tkn) {
                        case IdentifierToken it2 -> {
                            Identifier id2 = it2.asId();
                            if(scope.loadVar(id2, 2, actions, true)) {
                                srcReg = 2;
                            } else if(id2.starts("SysD")) {
                                srcReg = getSysDReg(id2);
                                if(srcReg == -1)
                                    throw ELAnalysisError.error("Unknown SysD variable "+id2, it2.span());
                            } else {
                                throw ELAnalysisError.error("Unknown variable "+id2, it2.span());
                            }
                        }
                        case NumberToken nt -> {
                            srcReg = 2;
                            actions.add(new DirectAction("LOAD r2 %d", ELValue.number(ELPrimitives.UINT32, nt).value));
                        }
                        default -> {
                        }
                    }
                    String srcRegStr = MachineCode.translateReg(srcReg);

                    eI++;
                    if(exp.size() > eI+1) {
                        int wR = 3;
                        boolean add = false;
                        boolean sub = false;
                        tkn = exp.get(eI);
                        if(tkn instanceof OperatorToken ot3) {
                            add = ot3.type == OperatorToken.Type.ADD;
                            sub = ot3.type == OperatorToken.Type.SUB;
                        }
                        eI++;
                        while(eI < exp.size()) {
                            tkn = exp.get(eI);
                            eI++;
                            switch (tkn) {
                                case NumberToken nt -> {
                                    int val = ELValue.number(ELPrimitives.UINT32, nt).value;
                                    if(sub) {
                                        val *= -1;
                                    }
                                    actions.add(new DirectAction("INC %s %d", srcRegStr, val));
                                }
                                case OperatorToken ot3 -> {
                                    add = ot3.type == OperatorToken.Type.ADD;
                                    sub = ot3.type == OperatorToken.Type.SUB;
                                }
                                case IdentifierToken it4 -> {
                                    Identifier id4 = it4.asId();
                                    String wRStr = MachineCode.translateReg(wR);
                                    if(scope.loadVar(id4, wR, actions, true)) {
                                        if(add) {
                                            actions.add(new DirectAction("ADD %s %s %s", srcRegStr, srcRegStr, wRStr));
                                        } else if(sub) {
                                            actions.add(new DirectAction("SUB %s %s %s", srcRegStr, srcRegStr, wRStr));
                                        }

                                    } else if(id4.starts("SysD")) {
                                        int r = getSysDReg(id4);
                                        if(r == -1)
                                            throw ELAnalysisError.error("Unknown SysD variable "+id4, it4.span());
                                        String tReg = MachineCode.translateReg(r);
                                        if(add) {
                                            actions.add(new DirectAction("ADD %s %s %s", srcRegStr, srcRegStr, tReg));
                                        } else if(sub) {
                                            actions.add(new DirectAction("SUB %s %s %s", srcRegStr, srcRegStr, tReg));
                                        }
                                    } else {
                                        throw ELAnalysisError.error("Unknown variable "+id4, it4.span());
                                    }
                                }
                                default -> {
                                }
                            }
                        }
                    }
                    actions.add(new DirectAction("STORE %s r1", srcRegStr));
                }
            }
            } catch (ELAnalysisError e) {
                errors.add(e);
            }
            wI++;
        }
        if(scope.getStackOffDif() > 0)
            actions.add(scope.getStackResetAction());
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
            case "rIR" -> MachineCode.REG_INTR_RSP;
            case "rPM" -> MachineCode.REG_PRIVILEGED_MODE;
            default -> -1;
        };
    }

    @Override
    public String toAssembly() {
        String s = "";
        for(Action a : actions)
            s+= a.toAssembly()+"\n";
        return s;
    }

}
