package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.*;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.ResolveAction;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.base.SysD;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class VariableNode extends ExpressionNode {

    public IdentifierToken token;
    public ELVariable variable;
    public boolean addressOf = false;
    protected ResolveAction rA = null;

    public VariableNode(ActionScope scope, IdentifierToken token) {
        super(scope);
        this.token = token;
    }

    @Override
    public String printTree() {
        return token.debugString();
    }

    @Override
    public String printNode() {
        return token.debugString();
    }

    @Override
    public boolean isConstant() {
        return variable != null && rA.wasConst;
    }

    @Override
    public int getConstant() {
        if(variable == null) {
            return 0;
        }
        return ((ELValue.ELNumberValue)variable.startingValue).value;
    }

    @Override

    public boolean validate(ErrorSet errors) {
        if(token.value.equals("SysD")) {
            switch(token.next().value) {
                case "rPgm", "rStack", "rPID", "rMTbl", "rPM", "rIC", "rIH", "rID", "rPgmI", "rStackI", "rPIDI", "rMTblI", "rPMI" -> {return true;}
                default -> {
                    if(token.next().value.matches("r\\d\\d?I?")) {
                        return true;
                    }
                }
            }
            // errors.error(String.format("Unable to resolve variable %s", token.debugString()), span());
            // return false;
        }
        rA = scope.loadVar(token, register, true);
        if(rA != null) {
            variable = rA.returnVar;
        }
        if(variable == null) {
            errors.error(String.format("Unable to resolve variable %s", token.debugString()), span());
            return false;
        }
        return true;
    }


    @Override
    public ELType getType() {
        if(token.value.equals("SysD") && rA == null) {
            return SysD.getVarType(token.next());
        }
        if(rA == null) {
            return ELPrimitives.UINT32;
        }
        return rA.returnType;
    }

    @Override
    public Span span() {
        return token.span();
    }

    @Override
    public String toAssembly() {
        if(token.value.equals("SysD") && rA == null) {
            return String.format("COPY %s %s", token.next().value, register);
        }
        return scope.loadVar(token, register, !addressOf).toAssembly();
    }
}