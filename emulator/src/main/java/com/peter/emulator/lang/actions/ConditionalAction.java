package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.Token;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.NumberToken;
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
            scope.loadVar(it1.asId(), 1, actions, true);
        } else if (t1 instanceof NumberToken nt1) {
            actions.add(new DirectAction("LOAD r1 %d", nt1.numValue));
        }
        if(t2 instanceof IdentifierToken it2) {
            scope.loadVar(it2.asId(), 2, actions, true);
        } else if (t2 instanceof NumberToken nt2) {
            actions.add(new DirectAction("LOAD r2 %d", nt2.numValue));
        }
        switch (ot.type) {
            case EQ2 -> actions.add(new DirectAction("SUB r1 r1 r2\nGOTO EQ r1 %s", trueTarget));
            case LEQ -> actions.add(new DirectAction("SUB r1 r1 r2\nGOTO LEQ r1 %s", trueTarget));
            case ANGLE_LEFT -> actions.add(new DirectAction("SUB r1 r2 r1\nGOTO GT r1 %s", trueTarget));
            case GEQ -> actions.add(new DirectAction("SUB r1 r2 r1\nGOTO LEQ r1 %s", trueTarget));
            case ANGLE_RIGHT -> actions.add(new DirectAction("SUB r1 r1 r2\nGOTO GT r1 %s", trueTarget));
            case NEQ -> actions.add(new DirectAction("SUB r1 r1 r2\nGOTO NEQ r1 %s", trueTarget));

            default -> throw ELAnalysisError.error("Unknown conditional", ot.span());
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
