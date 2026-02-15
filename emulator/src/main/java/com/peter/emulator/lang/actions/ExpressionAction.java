package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.NumberToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.SetToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ExpressionAction extends Action {

    public ArrayList<Action> actions = new ArrayList<>();
    public int targetReg;
    public ELType outType = null;
    
    public ExpressionAction(ActionScope scope, ArrayList<Token> tokens, int targetReg) {
        super(scope);
        String tRegStr = MachineCode.translateReg(targetReg);
        int wI = 0;

        OperatorToken.Type lastOp = null;
        ELType lastType = null;

        boolean addressOf = false;
        boolean resolvePointer = false;
        boolean not = false;

        while (wI < tokens.size()) {
            Token tkn = tokens.get(wI);
            switch (tkn) {
                case OperatorToken ot -> {
                    switch(ot.type) {
                        case ADD -> lastOp = ot.type;
                        case SUB -> lastOp = ot.type;
                        
                        case BITWISE_OR -> lastOp = ot.type;
                        case BITWISE_NOR -> lastOp = ot.type;
                        case BITWISE_AND -> {
                            if (lastOp != null) {
                                addressOf = true;
                            } else {
                                lastOp = ot.type;
                            }
                        }

                        case LEFT_SHIFT -> lastOp = ot.type;
                        case RIGHT_SHIFT -> lastOp = ot.type;

                        case POINTER -> {
                            if (lastOp != null) {
                                resolvePointer = true;
                            } else {
                                lastOp = ot.type;
                            }
                        }
                        case NOT -> {
                            if (lastOp != null) {
                                not = true;
                            } else {
                                throw ELAnalysisError.error("Unknown or unsupported operation", tkn);
                            }
                        }
                        
                        case AND -> lastOp = ot.type;
                        case OR -> lastOp = ot.type;

                        case LEQ -> lastOp = ot.type;
                        case GEQ -> lastOp = ot.type;
                        case ANGLE_LEFT -> lastOp = ot.type;
                        case ANGLE_RIGHT -> lastOp = ot.type;
                        case NEQ -> lastOp = ot.type;
                        case EQ2 -> lastOp = ot.type;

                        default -> {
                            throw ELAnalysisError.error("Unknown or unsupported operation", tkn);
                        }
                    }
                }
                case NumberToken nt -> {
                    if(addressOf || resolvePointer)
                        throw ELAnalysisError.error("Invalid pointer operation", tkn);
                    if(not)
                        throw ELAnalysisError.error("Can't not a number literal", tkn);
                    if (lastType != null) {
                        if(!ELPrimitives.UINT32.canCastTo(lastType))
                            throw ELAnalysisError.error("Invalid type-cast (uint32 -> "+lastType.toString()+")", tkn);
                    }
                    int tR = (lastOp == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    if (lastOp != null) {
                        switch(lastOp) {
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
                                throw ELAnalysisError.error("Unknown or unsupported operation", tkn);
                            }
                        }
                    } else {
                        actions.add(new DirectAction("LOAD %s %d", str, nt.numValue));
                        scope.reserve(tR);
                    }
                }
                case IdentifierToken it -> {
                    int tR = (lastOp == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    scope.loadVar(it, tR, actions, !addressOf);
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
                                throw ELAnalysisError.error("Unknown or unsupported operation", tkn);
                            }
                        }
                        scope.release(tR);
                    } else {
                        scope.reserve(tR);
                    }
                }
                case SetToken st -> {
                    // but what if this is casting?
                    int tR = (lastOp == null) ? targetReg : scope.firstFree();
                    String str = MachineCode.translateReg(tR);
                    actions.add(new ExpressionAction(scope, st.subTokens, tR));
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
                                throw ELAnalysisError.error("Unknown or unsupported operation", tkn);
                            }
                        }
                        scope.release(tR);
                    } else {
                        scope.reserve(tR);
                    }
                }
                default -> {
                    throw ELAnalysisError.error("Unexpected token found in expression: "+tkn, tkn);
                }
            }
            wI++;
        }

    }

    @Override
    public String toAssembly() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toAssembly'");
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
