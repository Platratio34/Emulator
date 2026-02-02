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

    public void parse(ArrayList<Token> tokens) {
        int wI = 0;
        int l = 0;
        actions.add(new DirectAction("COPY rStack r15"));
        int last = -1;
        while(wI < tokens.size()) {
            Token tkn = tokens.get(wI);
            System.out.println(tkn);
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
                            innerBlock.parse(tokens.get(wI+2).subTokens);
                            continue;
                        }
                        case "for" ->                             {
                            // set is (initilizer; condition; incrementer)
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens);
                            continue;
                        }
                        case "while" ->                             {
                            //set is condition
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens);
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
                                    if(it2.value.equals("SysD")) {
                                        String v2 = ((IdentifierToken)it2.subTokens.get(0)).value;
                                        if(v2.equals("rPgm")) {
                                            actions.add(new DirectAction("STACK PUSH rPgm"));
                                            types.add(ELPrimitives.UINT32);
                                        }
                                    } else if(scope.hasVariable(id2)) {
                                        scope.loadVar(id2, 2, actions);
                                        actions.add(new DirectAction("LOAD MEM r2 r2"));
                                        actions.add(new DirectAction("STACK PUSH r2"));
                                        types.add(scope.getVar(id2).type);
                                    } else {
                                        ELVariable var = scope.namespace.getVar(id2);
                                        if(var != null) {
                                            actions.add(new ResolveAction(2, var).byVal());
                                            actions.add(new DirectAction("STACK PUSH r2"));
                                            types.add(var.type);
                                        }
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
                                throw new ELCompileException("Unknown function "+id.fullName+tStr);
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
                        throw new ELCompileException("Expected identifier (found "+tkn+") @"+tkn.startLocation);
                    }
                    tkn = tokens.get(wI);
                    scope.addStackVar(name, type);
                    if(tkn instanceof OperatorToken ot) {
                        switch (ot.type) {
                            case SEMICOLON -> {
                                wI++;
                                actions.add(new StackAllocAction(-1));
                            }
                            case ASSIGN -> wI++;
                            default -> throw new ELCompileException("Expected `;` or `=` (found `"+ot.type+"`) @"+tkn.startLocation);
                        }
                    } else {
                        throw new ELCompileException("Expected `;` or `=` (found "+tkn+") @"+tkn.startLocation);
                    }
                    continue;
                }
                Identifier targetVal = it.asId();
                wI++;
                tkn = tokens.get(wI);
                if(tkn instanceof OperatorToken ot && (ot.type == OperatorToken.Type.ASSIGN || ot.type == OperatorToken.Type.INC || ot.type == OperatorToken.Type.DEC)) {                    
                    if(scope.hasVariable(targetVal)) { // block stack var
                        scope.loadVar(targetVal, 1, actions);
                    } else {
                        ELVariable var = scope.namespace.getVar(targetVal);
                        if(var != null) {
                            actions.add(new ResolveAction(1, var));
                        }
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
                        System.out.println("- "+tkn);
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
                            if(id2.starts("SysD")) {
                                if(id2.partEquals(1, "rPgm")) {
                                    srcReg = MachineCode.REG_PGM_PNTR;
                                }
                            } else if(scope.hasVariable(id2)) {
                                srcReg = 2;
                                scope.loadVar(id2, srcReg, actions);
                                actions.add(new DirectAction("LOAD MEM r2 r2"));
                            } else {
                                srcReg = 2;
                                ArrayList<ELVariable> varStack = scope.namespace.getVarStack(id2, new ArrayList<>());
                                if(!varStack.isEmpty()) {
                                    actions.add(new ResolveAction(2, varStack).byVal());
                                }
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
                                    if(id4.starts("SysD")) {
                                        if(id4.partEquals(1, "rPgm")) {
                                            if(add) {
                                                actions.add(new DirectAction("ADD %s %s rPgm", srcRegStr, srcRegStr));
                                            } else if(sub) {
                                                actions.add(new DirectAction("SUB %s %s rPgm", srcRegStr, srcRegStr));
                                            }
                                        }
                                    } else if(scope.hasVariable(id4)) {
                                        scope.loadVar(id4, wR, actions);
                                        String wRStr = MachineCode.translateReg(wR);
                                        actions.add(new DirectAction("LOAD MEM %s %s", wRStr, wRStr));
                                        if(add) {
                                            actions.add(new DirectAction("ADD %s %s %s", srcRegStr, srcRegStr, wRStr));
                                        } else if(sub) {
                                            actions.add(new DirectAction("SUB %s %s %s", srcRegStr, srcRegStr, wRStr));
                                        }
                                    } else {
                                        ELVariable var = scope.namespace.getVar(id4);
                                        if(var != null) {
                                            actions.add(new ResolveAction(wR, var).byVal());
                                        }
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
            wI++;
        }
        if(scope.getStackOffDif() > 0)
            actions.add(scope.getStackResetAction());
    }

    @Override
    public String toAssembly() {
        String s = "";
        for(Action a : actions)
            s+= a.toAssembly()+"\n";
        return s;
    }

}
