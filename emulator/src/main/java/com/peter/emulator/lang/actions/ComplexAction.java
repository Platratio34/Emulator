package com.peter.emulator.lang.actions;

import java.util.ArrayList;

public class ComplexAction extends Action {

    public final ArrayList<Action> actions = new ArrayList<>();

    public ComplexAction(ActionScope scope) {
        super(scope);
    }

    @Override
    public String toAssembly() {
        String out = "";
        boolean f = true;
        for (Action action : actions) {
            out += (f ? "" : "\n") + action.toAssembly();
            f = false;
        }
        return out;
    }

    public void addDirect(String asm, Object... args) {
        actions.add(new DirectAction(asm, args));
    }
    public void addDirect(String asm) {
        actions.add(new DirectAction(asm));
    }

}
