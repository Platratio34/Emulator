package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.StringToken;

public class StringNode extends ExpressionNode {

    protected static int stringI = 0;

    protected final StringToken token;
        
    public StringNode(ActionScope scope, StringToken token) {
        super(scope);
        this.token = token;
    }

    @Override
    public String printTree() {
        return token.escapedValue();
    }

    @Override
    public String printNode() {
        return token.escapedValue();
    }

    @Override
    public boolean isConstant() {
        return token.ch;
    }

    @Override
    public int getConstant() {
        return token.value.charAt(0);
    }

    @Override
    public ELType getType() {
        if(token.ch)
            return ELPrimitives.CHAR;
        return ELPrimitives.CHAR.pointerTo();
    }

    @Override
    public boolean validate(ErrorSet errors) {
        return true;
    }

    @Override
    public Span span() {
        return token.span();
    }

    @Override
    public String toAssembly() {
        if(token.ch) {
            return String.format("LOAD %s '%s'", register, token.escapedValue());
        }
        String id = String.format("exp_str_inline_%d", stringI++);
        return String.format("#define %s \"%s\"\nLOAD %s %s", id, token.escapedValue(), register, id);
    }

}
