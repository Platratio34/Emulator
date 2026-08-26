package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;

public class SubNode extends ExpressionNode {

    public Expression expression;

    public SubNode(Expression expression) {
        super(expression.scope);
        this.expression = expression;
    }

    @Override
    public String printTree() {
        return expression.printTree();
    }

    @Override
    public String printNode() {
        return "( " + expression.printNodes() + " )";
    }

    @Override
    public boolean isConstant() {
        return expression.isConstant();
    }
    
    @Override
    public int getConstant() {
        return expression.getConstant();
    }

    @Override
    public boolean validate(ErrorSet errors) {
        return expression.validate(errors);
    }

    @Override
    public ELType getType() {
        return expression.getType();
    }

    @Override
    public void setFalseTarget(String falseTarget) {
        expression.setFalseTarget(falseTarget);
    }
    @Override
    public void setTrueTarget(String trueTarget) {
        expression.setTrueTarget(trueTarget);
    }
    @Override
    public boolean hasGoto() {
        return expression.hadGoto();
    }

    @Override
    public Span span() {
        return expression.span();
    }

    @Override
    public String toAssembly() {
        expression.setRegister(register);
        return expression.toAssembly();
    }

}
