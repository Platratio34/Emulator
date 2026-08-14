package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.function.Function;

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
            String asm = action.toAssembly();
            if(asm == null || asm.length() == 0)
                continue;
            out += (f ? "" : "\n") + asm;
            f = false;
        }
        return out;
    }

    public void add(Action action) {
        actions.add(action);
    }

    public void addDirect(String asm, Object... args) {
        actions.add(new DirectAction(asm, args));
    }
    public void addDirect(String asm) {
        actions.add(new DirectAction(asm));
    }

    public void addReserve(Register register) {
        actions.add(new RegisterAction(scope, register, false));
    }
    public void addFind(Register register) {
        actions.add(new RegisterAction(scope, register, false, true));
    }
    public void addRelease(Register register) {
        actions.add(new RegisterAction(scope, register, true));
    }

    public void add(Function<ActionScope, String> onCompile) {
        actions.add(new CompilerAction(scope, onCompile));
    }

}
