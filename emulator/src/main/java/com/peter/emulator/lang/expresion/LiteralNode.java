package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.NumberToken;
import com.peter.emulator.lang.tokens.Token;

public class LiteralNode extends ExpressionNode {
    public int value;
    public ELType type = ELPrimitives.UINT32;

    public Token token;

    public LiteralNode(ActionScope scope, NumberToken token) {
        super(scope);
        this.value = token.numValue;
        this.token = token;
    }
    public LiteralNode(ActionScope scope, boolean value, Token token) {
        super(scope);
        this.value = value ? 1 : 0;
        type = ELPrimitives.BOOL;
        this.token = token;
    }
    public LiteralNode(ActionScope scope, int value, ELType type, Token token) {
        super(scope);
        this.value = value;
        this.type = type;
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
        return true;
    }

    @Override
    public int getConstant() {
        return value;
    }

    @Override
    public boolean validate(ErrorSet errors) {
        return true;
    }

    @Override
    public ELType getType() {
        return type;
    }

    @Override
    public Span span() {
        return token.span();
    }
    @Override
    public String toAssembly() {
        return String.format("LOAD %s %d", register, value);
    }
}