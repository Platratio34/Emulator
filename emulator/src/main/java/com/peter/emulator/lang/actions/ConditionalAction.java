package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.OperatorToken;

public class ConditionalAction extends Action {

    public final ActionScope scope;
    public final String trueTarget;
    public final String falseTarget;
    public final ArrayList<Action> actions = new ArrayList<>();

    public ConditionalAction(ActionScope scope, String trueTarget, String falseTarget, ArrayList<Token> condition) {
        this.scope = scope;
        this.trueTarget = trueTarget;
        this.falseTarget = falseTarget;
        Token t1 = condition.get(0);
        OperatorToken ot = (OperatorToken)condition.get(1);
        Token t2 = condition.get(2);
        if(t1 instanceof IdentifierToken it1) {
            
        }
        if(t2 instanceof IdentifierToken it2) {
            
        }
        actions.add(new DirectAction("GOTO %s", falseTarget));
    }

    @Override
    public String toAssembly() {
        String o = "";
        for(Action a : actions) {
            o += a.toAssembly()+"\n";
        }
        return o;
    }
}
