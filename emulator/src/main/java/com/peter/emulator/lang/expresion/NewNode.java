package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.NewAction;
import com.peter.emulator.lang.actions.Register;
import com.peter.emulator.lang.symbols.SymbolType;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class NewNode extends ExpressionNode {

    private final IdentifierToken nIt;
    private final IdentifierToken it;
    private final NewAction action;

    public NewNode(ActionScope scope, IdentifierToken nIt, IdentifierToken it) {
        super(scope);
        this.nIt = nIt;
        scope.addSymbol(SymbolType.KEYWORD, nIt.span());
        this.it = it;
        action = new NewAction(scope, it, new Register(scope));
    }

    @Override
    public String printTree() {
        return "new " + it.toString();
    }

    @Override
    public String printNode() {
        return "(new " + it.toString() + ")";
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
        return true;
    }

    @Override
    public Span span() {
        return nIt.startLocation.span(it.endLocation);
    }

    @Override
    public String toAssembly() {
        NewAction action = new NewAction(scope, it, register);
        return action.toAssembly();
    }

}
