package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELCompileException;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELValue;
import com.peter.emulator.lang.Token;
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
        while(wI < tokens.size()) {
            Token tkn = tokens.get(wI);
            System.out.println(tkn);
            actions.add(new DirectAction("// "+ (l++)+" "+tkn.startLocation.line()+":"+tkn.startLocation.col()));
            if(tkn instanceof IdentifierToken it) {
                if(tokens.get(wI+1) instanceof SetToken st) {
                    switch (it.value) {
                        case "if" ->                             {
                            // set is the condition
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens);
                        }
                        case "for" ->                             {
                            // set is (initilizer; condition; incrementer)
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens);
                        }
                        case "while" ->                             {
                            //set is condition
                            // also block
                            ActionBlock innerBlock = new ActionBlock(scope.createChild());
                            innerBlock.parse(tokens.get(wI+2).subTokens);
                        }
                        default -> {
                            // function call; set is parameters
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
                    if(scope.stackOff == 0) {
                        actions.add(new DirectAction("COPT rStack r15"));
                    }
                    scope.addStackVar(name, type);
                    if(tkn instanceof OperatorToken ot) {
                        switch (ot.type) {
                            case SEMICOLON -> {
                                wI++;
                                actions.add(new StackAllocAction(-1));
                                continue;
                            }
                            case ASSIGN -> wI++;
                            default -> throw new ELCompileException("Expected `;` or `=` (found `"+ot.type+"`) @"+tkn.startLocation);
                        }
                    } else {
                        throw new ELCompileException("Expected `;` or `=` (found "+tkn+") @"+tkn.startLocation);
                    }
                    continue;
                }
                IdentifierToken targetVal = it;
                wI++;
                tkn = tokens.get(wI);
                if(tkn instanceof OperatorToken ot && (ot.type == OperatorToken.Type.ASSIGN || ot.type == OperatorToken.Type.INC || ot.type == OperatorToken.Type.DEC)) {                    
                    if(targetVal.subTokens == null) { // block or namespace var
                        if(scope.hasVariable(targetVal)) { // block stack var
                            scope.loadVar(targetVal, 1, actions);
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
                    
                    if(tkn instanceof IdentifierToken it2) {
                        if(it2.value.equals("SysD")) {
                            String v2 = ((IdentifierToken)it2.subTokens.get(0)).value;
                            if(v2.equals("rPgm")) {
                                srcReg = MachineCode.REG_PGM_PNTR;
                            }
                        } else if(scope.hasVariable(it2)) {
                            srcReg = 2;
                            scope.loadVar(it2, srcReg, actions);
                            actions.add(new DirectAction("LOAD r2 r2"));
                        }
                    } else if(tkn instanceof NumberToken nt) {
                        srcReg = 2;
                        actions.add(new DirectAction("LOAD r2 %d", ELValue.number(ELPrimitives.UINT32, nt).value));
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
                            if(tkn instanceof NumberToken nt) {
                                int val = ELValue.number(ELPrimitives.UINT32, nt).value; 
                                if(sub) {
                                    val *= -1;
                                }
                                actions.add(new DirectAction("INC %s %d", srcRegStr, val));
                            } else if(tkn instanceof OperatorToken ot3) {
                                add = ot3.type == OperatorToken.Type.ADD;
                                sub = ot3.type == OperatorToken.Type.SUB;
                            } else if(tkn instanceof IdentifierToken it4) {
                                if(it4.value.equals("SysD")) {
                                    String v2 = ((IdentifierToken)it4.subTokens.get(0)).value;
                                    if(v2.equals("rPgm")) {
                                        if(add) {
                                            actions.add(new DirectAction("ADD %s %s rPgm", srcRegStr, srcRegStr));
                                        } else if(sub) {
                                            actions.add(new DirectAction("SUB %s %s rPgm", srcRegStr, srcRegStr));
                                        }
                                    }
                                } else if(scope.hasVariable(it4)) {
                                    scope.loadVar(it4, wR, actions);
                                    String wRStr = MachineCode.translateReg(wR);
                                    actions.add(new DirectAction("LOAD %s %s", wRStr, wRStr));
                                    if(add) {
                                        actions.add(new DirectAction("ADD %s %s %s", srcRegStr, srcRegStr, wRStr));
                                    } else if(sub) {
                                        actions.add(new DirectAction("SUB %s %s %s", srcRegStr, srcRegStr, wRStr));
                                    }
                                }
                            }
                        }
                    }
                    actions.add(new DirectAction("STORE %s r1", srcRegStr));
                }
            }
            wI++;
        }
    }

    @Override
    public String toAssembly() {
        String s = "";
        for(Action a : actions)
            s+= a.toAssembly()+"\n";
        return s;
    }

}
