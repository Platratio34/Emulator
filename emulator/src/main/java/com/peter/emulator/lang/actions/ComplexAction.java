package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.function.Function;

public class ComplexAction extends Action {

    public final ArrayList<Action> actions = new ArrayList<>();

    public ComplexAction(ActionScope scope) {
        super(scope);
    }

    protected static int globalIndex = 0;

    @Override
    public String toAssembly() {
        String out = "";
        boolean f = true;
        for (Action action : actions) {
            String asm = action.toAssembly();
            if (asm == null || asm.length() == 0)
                continue;
            out += (f ? "" : "\n") + asm;
            f = false;
        }
        if (out.contains("$i")) {
            for (int i = 0; i < 256; i++) {
                String id = "$i" + i;
                if (out.contains(id)) {
                    out = out.replace(id, ""+(globalIndex++));
                }
            }
        }
        if (out.contains("$r")) {
            boolean[] rs = new boolean[16];
            for (int i = 0; i < 16; i++) {
                String id = "$r" + i;
                if (out.contains(id)) {
                    int reg = scope.firstFreeR();
                    scope.reserve(reg);
                    rs[reg] = true;
                    out = out.replace(id, "r"+reg);
                }
            }
            for (int i = 1; i < 15; i++) {
                if(rs[i])
                    scope.release(i);
            }
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

    public boolean isEmpty() {
        return actions.isEmpty();
    }
}
