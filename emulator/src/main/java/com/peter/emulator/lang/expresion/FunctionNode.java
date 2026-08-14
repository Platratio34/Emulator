package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.FunctionAction;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class FunctionNode extends ExpressionNode {

    protected final IdentifierToken token;
    protected final FunctionAction action;

    public FunctionNode(ActionScope scope, IdentifierToken token) {
        super(scope);
        this.token = token;
        action = new FunctionAction(scope, register, token);
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
        return false;
    }

    @Override
    public ELType getType() {
        return action.retType;
    }

    @Override
    public boolean validate(ErrorSet errors) {
        return action != null;
    }

    @Override
    public Span span() {
        return token.span();
    }

    @Override
    public String toAssembly() {
        return new FunctionAction(scope, register, token).toAssembly();
    }

}
