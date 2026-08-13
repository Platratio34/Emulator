package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;

public class SubNode extends ExpresionNode {

    public Expression expresion;

    public SubNode(Expression expresion) {
        super(expresion.scope);
        this.expresion = expresion;
    }

    @Override
    public String printTree() {
        return expresion.printTree();
    }

    @Override
    public String printNode() {
        return "( " + expresion.printNodes() + " )";
    }

    @Override
    public boolean isConstant() {
        return expresion.isConstant();
    }
    
    @Override
    public int getConstant() {
        return expresion.getConstant();
    }

    @Override
    public boolean validate(ErrorSet errors) {
        return expresion.validate(errors);
    }

    @Override
    public ELType getType() {
        return expresion.getType();
    }

    @Override
    public Span span() {
        return expresion.span();
    }

    @Override
    public String toAssembly() {
        expresion.setRegister(register);
        return expresion.toAssembly();
    }

}
