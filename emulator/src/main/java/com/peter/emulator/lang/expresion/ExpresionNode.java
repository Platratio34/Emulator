package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.Action;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.Register;

public abstract class ExpresionNode extends Action {

    public ExpresionNode(ActionScope scope) {
        super(scope);
    }

    public ExpresionNode child1 = null;
    public ExpresionNode child2 = null;
    public boolean single = false;
    public Register register = null;

    public abstract String printTree();
    public abstract String printNode();

    public abstract boolean isConstant();
    public int getConstant() { return 0; }

    public abstract ELType getType();

    public abstract boolean validate(ErrorSet errors);

    public abstract Span span();
}
